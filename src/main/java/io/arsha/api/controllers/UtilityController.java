package io.arsha.api.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.arsha.api.data.scraper.ScrapedItem;
import io.arsha.api.exceptions.InvalidLocaleException;
import io.arsha.api.services.ScraperService;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@Validated
@RestController
@RequestMapping("/util")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class UtilityController {

    final ObjectMapper mapper = new ObjectMapper();
    private final ScraperService scraperService;

    @GetMapping(value = "/db", produces = "application/json")
    protected String getScrapedItem(
            @Nullable @RequestParam(required = false) @Size(min = 2, max = 2) String lang,
            @Nullable @RequestParam(required = false) @Size(max = 100) Set<Long> id
    ) throws InvalidLocaleException, JsonProcessingException {
        var locale = lang == null ? "en" : lang.toLowerCase();
        var entireDb = id == null || id.isEmpty();

        List<ScrapedItem> items;
        if (entireDb) {
            items = scraperService.getAll(locale);
            return mapper.writeValueAsString(items);
        } else {
            items = scraperService.getMany(locale, id.stream().toList());
            if (items.size() != 1) {
                return mapper.writeValueAsString(items);
            } else {
                return mapper.writeValueAsString(items.get(0));
            }
        }

    }

}
