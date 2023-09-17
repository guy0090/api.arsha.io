package io.arsha.api.services;

import io.arsha.api.data.CacheCompositeKey;
import io.arsha.api.data.market.common.MarketEndpoint;
import io.arsha.api.data.market.MarketResponse;
import io.arsha.api.data.market.responses.ParsedItems;
import io.arsha.api.data.market.responses.RawItems;
import io.arsha.api.exceptions.AbstractException;
import io.arsha.api.exceptions.MarketRequestException;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.util.function.Tuple2;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MarketService {

    private final MarketRequestService marketRequestService;
    private final MarketRedisService marketRedisService;
    private final ScraperService scraperService;

    public RawItems requestOne(CacheCompositeKey key) throws MarketRequestException {
        var cachedResult = marketRedisService.get(key);
        if (cachedResult.isPresent()) return new RawItems(cachedResult.get());

        var future = marketRequestService.request(key).join();
        var response = future.getT2().orElseThrow(() -> new MarketRequestException(key.getPrimary()));
        if (key.getEndpoint() == MarketEndpoint.MARKET_WAIT_LIST) {
            return new RawItems(response); // Don't cache wait lists, they change too often
        }

        marketRedisService.setDefaultExpireAsync(key, response);
        return new RawItems(response);
    }

    // This is async, will max out at 100 parallel requests
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public RawItems requestMany(List<CacheCompositeKey> keys) {
        var results = marketRedisService.getMany(keys);

        var futures = new ArrayList<CompletableFuture<Tuple2<CacheCompositeKey, Optional<MarketResponse>>>>(); // hell is a generic
        for (var entry : results.entrySet()) {
            if (entry.getValue() != null) continue;
            var key = entry.getKey();
            var response = marketRequestService.request(key);
            futures.add(response);
        }

        if (!futures.isEmpty()) {
            completeFutures(results, futures.toArray(CompletableFuture[]::new));
        }

        return RawItems.fromResults(results);
    }

    // Special case to handle the search endpoint while still using the cache properly
    public RawItems requestSearchResult(CacheCompositeKey key) throws MarketRequestException {
        if (key.getSearch() == null) throw new MarketRequestException(); // This should never happen

        // Convert each item ID to individual CacheCompositeKey
        var keys = Arrays.stream(key.getSearch().split(","))
                .filter(s -> !s.isBlank() && !s.equalsIgnoreCase("null"))
                .map(s -> CacheCompositeKey.builder()
                        .search(s)
                        .region(key.getRegion())
                        .endpoint(key.getEndpoint())
                        .build())
                .toList();

        // If any of the results are null, we need to request them from the API
        var results = marketRedisService.getMany(keys);
        var cacheMisses = results.entrySet().stream()
                .filter(e -> e.getValue() == null)
                .map(entry -> entry.getKey().getSearch())
                .collect(Collectors.toSet());

        if (!cacheMisses.isEmpty()) {
            // Handle the missing item IDs by fetching and caching them
            handleSearchResults(cacheMisses, key, keys, results);
        }

        // Combine the resulting MarketResponses into a single response to recreate the original search result
        return RawItems.fromResults(results).combinedResult("|");
    }

    // Special case to handle category requests and to allow grabbing all subcategories if not explicitly specified
    public RawItems requestCategoryResult(List<CacheCompositeKey> keys, boolean combinedResult) throws MarketRequestException {
        if (keys.size() == 1) {
            var result = marketRedisService.get(keys.get(0));
            if (result.isPresent()) return new RawItems(result.get());

            var future = marketRequestService.request(keys.get(0)).join();
            var response = future.getT2().orElseThrow(() -> new MarketRequestException(keys.get(0).getPrimary()));
            marketRedisService.setDefaultExpireAsync(keys.get(0), response);
            return new RawItems(response);
        }

        var results = requestMany(keys);
        return combinedResult ? results.combinedResult("") : results;
    }

    // Responsible for handling any V2 parsed item requests.
    public <R extends ParsedItems<?>> R requestParsedItem(CacheCompositeKey key, String lang) throws AbstractException {
        return requestParsedItems(List.of(key), lang);
    }

    public <R extends ParsedItems<?>> R requestParsedItems(List<CacheCompositeKey> keys, String lang) throws AbstractException {
        var result = ParsedItems.<R>getParsingClass(keys.get(0).getEndpoint());
        var locale = lang == null ? "en" : lang;
        var ids = extractIdsFromCacheKeys(keys);

        var endpoint = keys.get(0).getEndpoint();
        var responses = switch (endpoint) {
            case MARKET_LIST -> requestCategoryResult(keys, false); // can't re-assign sub category otherwise
            case MARKET_SEARCH_LIST -> requestSearchResult(keys.get(0));
            default -> requestMany(keys);
        };

        var scrapedItems = scraperService.getMappedScrapedItems(locale, ids);
        if (endpoint == MarketEndpoint.MARKET_LIST) scrapedItems.clear();

        for (var i = 0; i < responses.size(); i++) {
            var key = keys.get(i);
            var response = responses.get(i);
            if (response.hasBadData() && endpoint != MarketEndpoint.MARKET_WAIT_LIST) {
                throw new MarketRequestException(key);
            }

            var scraped = scrapedItems.get(key.getPrimary());
            var itemName = scraped == null ? "NONAME" : scraped.getName();

            result.addResponse(itemName, key, response);
        }

        if (scrapedItems.isEmpty() && !result.isEmpty()) {
            ids = result.getItemIds();
            scrapedItems = scraperService.getMappedScrapedItems(locale, ids);
            result.setNames(scrapedItems);
        }

        return result;
    }

    /**
     * Handles the completion of a list of futures, updating the provided results map in place.
     * <br><br>
     * There are two (?) cases in which a response will return with a resultMsg of "0":
     * <ul>
     *  <li>1. The item ID is invalid</li>
     *  <li>2. The item ID is valid, but there is no market data for it</li>
     * </ul>
     *
     * For now all values will be cached, if in future this should change, the following code can be used:
     * <pre>
     * if (!response.hasBadData()) marketRedisService.setDefaultExpireAsync(key, response);
     * </pre>
     */
    private void completeFutures(Map<CacheCompositeKey, MarketResponse> results, CompletableFuture<Tuple2<CacheCompositeKey, Optional<MarketResponse>>>[] futures)
            throws ExecutionException, InterruptedException, MarketRequestException {
        var start = System.currentTimeMillis();
        CompletableFuture.allOf(futures).join();

        for (var future : futures) {
            var tuple = future.get();
            var key = tuple.getT1();
            var response = tuple.getT2().orElseThrow(() -> new MarketRequestException(key));

            marketRedisService.setDefaultExpireAsync(key, response);
            results.put(key, response);
        }
        log.debug("{} requests completed after {}ms", futures.length, System.currentTimeMillis() - start);
    }

    // Handles the results of a search request and modifies the provided results map in place
    private void handleSearchResults(Set<String> cacheMisses, CacheCompositeKey key, List<CacheCompositeKey> keys, Map<CacheCompositeKey, MarketResponse> results) throws MarketRequestException {
        // Update the key to reflect the new search string (search = IDs of the missing items)
        var updatedKey = CacheCompositeKey.builder()
                .search(String.join(",", cacheMisses))
                .region(key.getRegion())
                .endpoint(key.getEndpoint())
                .build();
        log.debug("Search result requires {} cache misses, updating key to {}", cacheMisses.size(), updatedKey.getSearch());

        var future = marketRequestService.request(updatedKey).join();
        // If the request failed, throw an exception
        var response = future.getT2().orElseThrow(() ->
                new MarketRequestException("Search request failed for " + updatedKey.getSearch()));

        if (response.getResultMessage().equals("0")) {
            // Every item ID returned no data, so we throw an exception to avoid allowing any of them to be cached
            throw new MarketRequestException(updatedKey);
        }

        // Remap each part of the result to a MarketResponse and cache them individually
        var marketResponses = response.getResult().stream().map(MarketResponse::new).toList();
        for (var mResponse : marketResponses) {
            var tmpKey = keys.stream()
                    .filter(k -> {
                        assert k.getSearch() != null;
                        return mResponse.getResultMessage().startsWith(k.getSearch());
                    })
                    .findFirst()
                    .orElseThrow();
            marketRedisService.setDefaultExpireAsync(tmpKey, mResponse);
            results.put(tmpKey, mResponse);
        }

        log.debug("Search result cache updated with {}/{} new entries", marketResponses.size(), cacheMisses.size());

        // If any result's value remains null, throw an exception due to missing market data
        var keysWithMissingValue = results.entrySet().stream()
                .filter(e -> e.getValue() == null)
                .map(Map.Entry::getKey)
                .toList();
        if (!keysWithMissingValue.isEmpty()) {
            var keyForMissing = CacheCompositeKey.createCombinedSearchKey(keysWithMissingValue);
            throw new MarketRequestException(keyForMissing);
        }

    }

    protected List<Long> extractIdsFromCacheKeys(List<CacheCompositeKey> keys) {
        var ids = new ArrayList<Long>();
        keys.forEach(key -> ids.addAll(key.getPrimaryIds()));
        return ids;
    }
}
