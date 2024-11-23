package io.arsha.api.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.arsha.api.config.services.CodexConfigurationService;
import io.arsha.api.data.scraper.CodexData;
import io.arsha.api.data.scraper.ScrapedItem;
import io.arsha.api.exceptions.InvalidLocaleException;
import io.arsha.api.schedulers.ScraperScheduler.ExecutionType;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ScraperService {

    private final String hostName;
    private final RestTemplate httpClient;
    private final ScraperDataRedisService redisService;
    private final CodexConfigurationService codexConfigurationService;

    public void setLock() {
        redisService.setLock(hostName);
    }

    public void releaseLock() {
        getLockOwner().ifPresent(k -> {
            if (k.equals(hostName)) {
                log.info("Releasing lock '{}'", hostName);
                redisService.releaseLock();
            } else {
                log.debug("Attempted to release lock for host '{}' but lock is owned by '{}'", hostName, k);
            }
        });
    }

    public Optional<String> getLockOwner() {
        return redisService.getLockOwner();
    }

    public boolean isLockOwner() {
        return getLockOwner().map(k -> k.equals(hostName)).orElse(false);
    }

    public Optional<Integer> scrape(String locale, ExecutionType executionType) {
        var data = requestItems(locale, executionType);
        if (data.isEmpty()) return Optional.empty();

        var items = scrapeItems(data.get());
        saveItems(locale, items);

        return Optional.of(items.size());
    }

    public Optional<CodexData> requestItems(String locale, ExecutionType executionType) {
        // If called during startup, validate if a new scrape is needed
        // If called during a scheduled or forced run, always scrape
        var scrapeNeeded = !executionType.isStartup() || isScrapeNeeded(locale);
        if (!scrapeNeeded) return Optional.empty();

        try {
            var url = codexConfigurationService.getItemsEndpoint(locale);
            var response = httpClient.getForObject(url, String.class); // Returned content type is text/html and not application/json
            if (response == null) return Optional.empty();

            return Optional.of(
                    new ObjectMapper().readValue(response.substring(1), CodexData.class));
        } catch (Exception e) {
            log.error("Error requesting items from codex", e);
            return Optional.empty();
        }
    }

    public List<ScrapedItem> scrapeItems(CodexData data) {
        var scrapedResults = data.aaData();
        var items = scrapedResults.stream().map(CodexData::buildItem).collect(Collectors.toList());
        items.add(new ScrapedItem(45868L, "Signature Classic Box", 4));
        items.add(new ScrapedItem(601251L, "Value Pack (30 Days)", 3));

        return items;
    }

    public void saveItems(String locale, List<ScrapedItem> items) {
        log.debug("Saving {} scraped items to redis", items.size());
        redisService.putAllAsync(locale, items);
        redisService.saveLastScrapedTime(locale);
    }

    /**
     * Scrape is needed if no previous scrape has been done or if the last scrape was more than 6 days ago
     * A scheduled run is done every Friday at 1am
     */
    public boolean isScrapeNeeded(String locale) {
        var lastScrapedTime = redisService.getLastScrapedTime(locale).orElse(null);
        if (lastScrapedTime == null) return true;

        log.debug("Last scrape time for locale '{}': {}", locale, lastScrapedTime);
        return Instant.now().isAfter(lastScrapedTime.plusDays(6).toInstant());
    }

    public Optional<ScrapedItem> getScrapedItem(String locale, Long id) {
        var itemId = String.valueOf(id);
        return redisService.get(locale, itemId);
    }

    public Map<Long, ScrapedItem> getMappedScrapedItems(String locale, List<Long> ids) {
        var scrapedItems = redisService.getMany(locale, ids);

        var items = new HashMap<Long, ScrapedItem>();
        for (var i = 0; i < ids.size(); i++) {
            var id = ids.get(i);
            var scrapedItem = scrapedItems.get(i);
            if (scrapedItem != null) items.put(id, scrapedItem);
        }

        return items;
    }

    public List<ScrapedItem> getMany(String locale, List<Long> ids) throws InvalidLocaleException {
        if (!codexConfigurationService.isValidLocale(locale)) {
            throw new InvalidLocaleException(locale);
        }
        return redisService.getMany(locale, ids);
    }

    public List<ScrapedItem> getAll(String locale) throws InvalidLocaleException {
        if (!codexConfigurationService.isValidLocale(locale)) {
            throw new InvalidLocaleException(locale);
        }
        return redisService.getAll(locale);
    }

}
