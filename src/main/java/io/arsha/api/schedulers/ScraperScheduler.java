package io.arsha.api.schedulers;

import io.arsha.api.config.services.CodexConfigurationService;
import io.arsha.api.services.ScraperService;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Profile("!test")
@Component
@RequiredArgsConstructor
public class ScraperScheduler {

    private final RedissonClient redisson;
    private final ScraperService scraperService;
    private final CodexConfigurationService codexConfigurationService;

    @EventListener(ApplicationReadyEvent.class)
    public void runScrapeAtStartup() {
        scrapeAll(ExecutionType.STARTUP);
    }

    @Scheduled(cron = "0 0 1 * * FRI")
    private void scrape() {
        scrapeAll(ExecutionType.CRON);
    }

    private Optional<RLock> acquireLock() {
        var lock = redisson.getLock("scraperLock");
        if (lock.tryLock()) {
            return Optional.of(lock);
        }
        return Optional.empty();
    }

    private void scrapeAll(ExecutionType executionType) {
        var lock = acquireLock();
        if (lock.isEmpty()) {
            log.info("Scraper is already running, skipping this execution");
            return;
        }

        try {
            var locales = codexConfigurationService.getLocales();
            var start = Instant.now();
            for (var locale : locales) {
                log.info("Scraping locale {}", locale);
                var count = scraperService.scrape(locale, executionType);
                count.ifPresentOrElse(
                        c -> log.info("Scraped {} items from locale {}", c, locale),
                        () -> log.info("Skipped scrape for locale {}", locale));
            }
            log.info("Finished scheduled scrape of all locales in {} seconds", Duration.between(start, Instant.now()).getSeconds());
        } catch (Exception e) {
            log.error("Error scraping all locales", e);
        } finally {
            lock.get().unlock();
        }
    }

    public enum ExecutionType {
        STARTUP,
        CRON,
        FORCED;

        public boolean isStartup() {
            return this == STARTUP;
        }
    }
}
