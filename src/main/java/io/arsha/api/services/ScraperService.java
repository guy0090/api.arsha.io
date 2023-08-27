package io.arsha.api.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.arsha.api.config.services.CodexConfigurationService;
import io.arsha.api.data.scraper.CodexData;
import io.arsha.api.data.scraper.ScrapedItem;
import io.arsha.api.exceptions.InvalidLocaleException;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ScraperService {

    private final RestTemplate httpClient;
    private final ScraperDataRedisService redisService;
    private final CodexConfigurationService codexConfigurationService;

    static final Duration MIN_SCRAPE_INTERVAL = Duration.ofDays(6);

    @Nullable
    public Integer scrape(String locale, Boolean force) {
        var data = requestItems(locale, force);
        if (data == null) return null;

        var items = scrapeItems(data);
        saveItems(locale, items);

        return items.size();
    }

    @Nullable
    public CodexData requestItems(String locale, Boolean force) {
        var scrapeNeeded = force || isScrapeNeeded(locale);
        if (!scrapeNeeded) return null;

        try {
            var url = codexConfigurationService.getItemsEndpoint(locale);
            var response = httpClient.getForObject(url, String.class); // Returned content type is text/html and not application/json
            return new ObjectMapper().readValue(response.substring(1), CodexData.class);
        } catch (Exception e) {
            log.error("Error requesting items from codex", e);
            return null;
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

    public Boolean isScrapeNeeded(String locale) {
        var lastScrapedTime = redisService.getLastScrapedTime(locale);
        var timeSinceLastScrape = lastScrapedTime.map(time -> Duration.between(time, Instant.now()));
        return timeSinceLastScrape.isEmpty() || timeSinceLastScrape.get().compareTo(MIN_SCRAPE_INTERVAL) > 0;
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

    public Optional<Instant> getLastScrapedTime(String locale) {
        return redisService.getLastScrapedTime(locale);
    }

}
