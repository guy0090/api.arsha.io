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

    @Scheduled(cron = "0 0 1 * * THU")
    private void scrape() {
        scrapeAll(ExecutionType.CRON);
    }

    private void scrapeAll(ExecutionType executionType) {
        log.info("Starting scheduled scrape of all locales");
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
