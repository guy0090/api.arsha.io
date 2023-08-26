package io.arsha.api.controllers;

import io.arsha.api.data.scraper.ScrapedItem;
import io.arsha.api.services.ScraperService;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneId;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AdminController {

    private final ScraperService scraperService;

    @PostMapping("/lastScraped")
    protected LocalDateTime scrape(@RequestParam String locale) {
        var date = scraperService.getLastScrapedTime(locale).orElse(null);
        if (date == null) return null;

        return LocalDateTime.ofInstant(date, ZoneId.of("UTC"));
    }

    @PostMapping("/scrapedItem")
    protected ScrapedItem scrapedItem(@RequestParam String locale, @RequestParam String id) {
        var item = scraperService.getScrapedItem(locale, Long.valueOf(id));
        return item.orElse(null);
    }
}
