package io.arsha.api.schedulers;

import io.arsha.api.config.services.CodexConfigurationService;
import io.arsha.api.services.ScraperService;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Profile("!test")
@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ScraperScheduler {

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

    private void scrapeAll(ExecutionType executionType) {
        try {
            log.info("Starting scheduled scrape of all locales");
            var locales = codexConfigurationService.getLocales();

            scraperService.setLock();
            Thread.sleep(2000); // Slowest wins lock ownership
            if (!scraperService.isLockOwner()) {
                log.info("Skipping scrape because lock is owned by '{}'", scraperService.getLockOwner().orElse("unknown"));
                return;
            }

            var start = Instant.now();
            for (var locale : locales) {
                log.info("Scraping locale {}", locale);
                var count = scraperService.scrape(locale, executionType);
                count.ifPresentOrElse(
                        c -> log.info("Scraped {} items from locale {}", c, locale),
                        () -> log.info("Skipped scrape for locale {}", locale));
            }
            log.info("Finished scheduled scrape of all locales in {} seconds", Duration.between(start, Instant.now()).getSeconds());
        } catch (InterruptedException e) {
            log.error("Error scraping all locales", e);
            Thread.currentThread().interrupt();
        } finally {
            scraperService.releaseLock();
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
