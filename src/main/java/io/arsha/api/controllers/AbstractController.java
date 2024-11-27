package io.arsha.api.controllers;

import io.arsha.api.config.services.MarketConfigurationService;
import io.arsha.api.data.CacheCompositeKey;
import io.arsha.api.data.market.IMarketResponse;
import io.arsha.api.data.market.common.GameRegion;
import io.arsha.api.data.market.common.MarketEndpoint;
import io.arsha.api.data.market.responses.MarketResponses;
import io.arsha.api.data.rest.Category;
import io.arsha.api.data.rest.IdAndSid;
import io.arsha.api.data.rest.Ids;
import io.arsha.api.exceptions.AbstractException;
import io.arsha.api.exceptions.IdAndSidMismatchException;
import io.arsha.api.exceptions.MarketRegionException;
import io.arsha.api.exceptions.MarketRequestException;
import io.arsha.api.services.MarketCategoryService;
import io.arsha.api.services.MarketService;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.NonNull;
import org.hibernate.validator.constraints.Length;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class AbstractController<T extends IMarketResponse> {

    @Inject
    MarketService marketService;
    @Inject
    MarketCategoryService categoryService;
    @Inject
    MarketConfigurationService marketConfigurationService;

    /// Endpoint Mappings

    // GET
    protected abstract MarketResponses<T> getWorldMarketSubList(String region, @Size(min = 1, max = 100) Set<Long> ids, @Length(max = 2) String lang)
            throws AbstractException;

    // POST
    protected abstract MarketResponses<T> getWorldMarketSubList(String region, @Valid @Size(min = 1, max = 100) Ids ids, @Length(max = 2) String lang)
            throws AbstractException;

    // GET
    protected abstract MarketResponses<T> getMarketPriceInfo(String region, @Size(min = 1, max = 100) List<Long> id, @Size(min = 1, max = 100) List<Long> sid, @Length(max = 2) String lang)
            throws AbstractException;

    // POST
    protected abstract MarketResponses<T> getMarketPriceInfo(String region, @Valid @Size(min = 1, max = 20) IdAndSid ids, @Length(max = 2) String lang)
            throws AbstractException;

    // GET
    protected abstract MarketResponses<T> getBiddingInfoList(String region, @Size(min = 1, max = 100) List<Long> id, @Size(min = 1, max = 100) List<Long> sid, @Length(max = 2) String lang)
            throws AbstractException;

    // POST
    protected abstract MarketResponses<T> getBiddingInfoList(String region, @Valid @Size(min = 1, max = 20) IdAndSid ids, @Length(max = 2) String lang)
            throws AbstractException;

    // GET & POST
    protected abstract MarketResponses<T> getWorldMarketWaitList(String region, @Length(max = 2) String lang)
            throws AbstractException;

    // GET & POST
    protected abstract MarketResponses<T> getWorldMarketHotList(String region, @Length(max = 2) String lang)
            throws AbstractException;

    // GET
    protected abstract MarketResponses<T> getWorldMarketList(String region, Long mainCategory, Long subCategory, @Length(max = 2) String lang)
            throws AbstractException;

    // POST
    protected abstract MarketResponses<T> getWorldMarketList(String region, @Valid Category category, @Length(max = 2) String lang)
            throws AbstractException;

    // GET
    protected abstract MarketResponses<T> getWorldMarketSearchList(String region, Set<Long> ids, @Length(max = 2) String lang)
            throws AbstractException;

    // POST
    protected abstract MarketResponses<T> getWorldMarketSearchList(String region, @Valid Ids ids, @Length(max = 2) String lang)
            throws AbstractException;

    /// Request methods

    protected CacheCompositeKey noArgumentRequest(@NonNull String region, @NonNull MarketEndpoint endpoint)
            throws MarketRegionException {
        var gameRegion = validateRegion(region);
        return createKey(null, null, null, gameRegion, endpoint);
    }

    protected List<CacheCompositeKey> idsRequest(@NonNull Set<Long> ids, String region) throws MarketRegionException {
        var gameRegion = validateRegion(region);
        return createKeys(ids.stream().toList(), gameRegion, MarketEndpoint.MARKET_SUB_LIST);
    }

    protected List<CacheCompositeKey> idsRequest(@NonNull Ids ids, String region) throws MarketRegionException {
        var gameRegion = validateRegion(region);
        return createKeys(ids.stream().toList(), gameRegion, MarketEndpoint.MARKET_SUB_LIST);
    }

    protected List<CacheCompositeKey> idAndSidRequest(String region, List<Long> id, List<Long> sid, MarketEndpoint endpoint)
            throws AbstractException {
        // We have no way of knowing what SID should belong to which ID if the lists are of different sizes.
        // This is only an issue with GET requests, as POST requests will have the ID and SID paired together.
        if (sid != null && sid.size() != id.size()) throw new IdAndSidMismatchException(id, sid);

        var idAndSid = IdAndSid.collect(id, sid);
        return idAndSidRequest(region, idAndSid, endpoint);
    }

    protected List<CacheCompositeKey> idAndSidRequest(String region, IdAndSid ids, MarketEndpoint endpoint)
            throws AbstractException {
        var gameRegion = validateRegion(region);
        return createKeys(ids, gameRegion, endpoint);
    }

    protected List<CacheCompositeKey> categoryRequest(Category category, @NonNull String region)
            throws MarketRegionException, MarketRequestException {
        var gameRegion = validateRegion(region);
        var mainCategory = category.getMainCategory();
        var subCategory = category.getSubCategory();

        if (!categoryService.isValidCombination(mainCategory, subCategory)) {
            throw new MarketRequestException(category);
        }

        if (subCategory == null) {
            var subCategories = categoryService.getSubCategories(mainCategory);
            return subCategories.stream().map(id -> createKey(null, mainCategory, id, gameRegion, MarketEndpoint.MARKET_LIST))
                    .toList();
        } else {
            var key = createKey(null, mainCategory, subCategory, gameRegion, MarketEndpoint.MARKET_LIST);
            return Collections.singletonList(key);
        }
    }

    protected CacheCompositeKey searchRequest(@NonNull Set<Long> ids, @NonNull String region)
            throws MarketRegionException {
        var gameRegion = validateRegion(region);
        var query = "";

        var stringList = ids.stream().map(String::valueOf).toList();
        if (stringList.size() == 1) {
            query = stringList.getFirst();
        } else {
            query = String.join(",", stringList);
        }

        return createKey(query, null, null, gameRegion, MarketEndpoint.MARKET_SEARCH_LIST);
    }

    /// Utility methods

    protected CacheCompositeKey createKey(@Nullable String search, @Nullable Long primary, @Nullable Long secondary,
                                          @NonNull GameRegion region, @NonNull MarketEndpoint endpoint) {
        return CacheCompositeKey.builder()
                .search(search)
                .primary(primary)
                .secondary(secondary)
                .region(region)
                .endpoint(endpoint)
                .build();
    }

    protected List<CacheCompositeKey> createKeys(List<Long> ids, GameRegion region, MarketEndpoint endpoint) {
        return ids.stream().map(id -> createKey(null, id, null, region, endpoint)).toList();
    }

    protected List<CacheCompositeKey> createKeys(IdAndSid body, GameRegion region, MarketEndpoint endpoint) {
        return body.stream().map(item -> createKey(null, item.getId(), item.getSid(), region, endpoint)).toList();
    }

    protected GameRegion validateRegion(String region) throws MarketRegionException {
        if (region == null) throw new MarketRegionException();
        var gameRegion = GameRegion.fromValue(region);
        if (gameRegion == null) throw new MarketRegionException(region);
        else if (!marketConfigurationService.isEnabled(gameRegion)) throw new MarketRegionException(gameRegion);
        return gameRegion;
    }
}
