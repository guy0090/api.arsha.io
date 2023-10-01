package io.arsha.api.controllers;

import io.arsha.api.data.CacheCompositeKey;
import io.arsha.api.data.market.common.MarketEndpoint;
import io.arsha.api.data.market.items.*;
import io.arsha.api.data.market.responses.ParsedList;
import io.arsha.api.data.rest.Category;
import io.arsha.api.data.rest.IdAndSid;
import io.arsha.api.data.rest.Ids;
import io.arsha.api.exceptions.AbstractException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Validated
@RestController
@RequestMapping("/v2/{region}")
public class V2Controller extends AbstractController<ParsedList> {

    @GetMapping(path = {"/GetWorldMarketSubList", "/item"})
    protected GetWorldMarketSubList getWorldMarketSubList(
            @PathVariable String region,
            @RequestParam Set<Long> id,
            @RequestParam(required = false) String lang
    ) throws AbstractException {
        var keys = idsRequest(id, region);
        return marketService.requestParsedItems(keys, lang);
    }

    @PostMapping(path = {"/GetWorldMarketSubList", "/item"})
    protected GetWorldMarketSubList getWorldMarketSubList(
            @PathVariable String region,
            @RequestBody Ids ids,
            @RequestParam(required = false) String lang
    ) throws AbstractException {
        var keys = idsRequest(ids, region);
        return marketService.requestParsedItems(keys, lang);
    }

    @GetMapping(path = {"/GetMarketPriceInfo", "/history"})
    protected GetMarketPriceInfo getMarketPriceInfo(
            @PathVariable String region,
            @RequestParam List<Long> id,
            @RequestParam(required = false) List<Long> sid,
            @RequestParam(required = false) String lang
    ) throws AbstractException {
        var keys = idAndSidRequest(region, id, sid, MarketEndpoint.MARKET_PRICE_INFO);
        return marketService.requestParsedItems(keys, lang);
    }

    @PostMapping(path = {"/GetMarketPriceInfo", "/history"})
    protected GetMarketPriceInfo getMarketPriceInfo(
            @PathVariable String region,
            @RequestBody IdAndSid ids,
            @RequestParam(required = false) String lang
    ) throws AbstractException {
        var keys = idAndSidRequest(region, ids, MarketEndpoint.MARKET_PRICE_INFO);
        return marketService.requestParsedItems(keys, lang);
    }

    @GetMapping(path = {"/GetBiddingInfoList", "/orders"})
    protected GetBiddingInfoList getBiddingInfoList(
            @PathVariable String region,
            @RequestParam List<Long> id,
            @RequestParam(required = false) List<Long> sid,
            @RequestParam(required = false) String lang
    ) throws AbstractException {
        var keys = idAndSidRequest(region, id, sid, MarketEndpoint.BIDDING_INFO_LIST);
        return marketService.requestParsedItems(keys, lang);
    }

    @PostMapping(path = {"/GetBiddingInfoList", "/orders"})
    protected GetBiddingInfoList getBiddingInfoList(
            @PathVariable String region,
            @RequestBody IdAndSid ids,
            @RequestParam(required = false) String lang) throws AbstractException {
        var keys = idAndSidRequest(region, ids, MarketEndpoint.BIDDING_INFO_LIST);
        return marketService.requestParsedItems(keys, lang);
    }

    @RequestMapping(path = {"/GetWorldMarketWaitList", "/queue"}, method = {RequestMethod.GET, RequestMethod.POST})
    protected GetWorldMarketWaitList getWorldMarketWaitList(
            @PathVariable String region,
            @RequestParam(required = false) String lang
    ) throws AbstractException {
        var key = noArgumentRequest(region, MarketEndpoint.MARKET_WAIT_LIST);
        return marketService.requestParsedItem(key, lang);
    }

    @RequestMapping(path = {"/GetWorldMarketHotList", "/hot"}, method = {RequestMethod.GET, RequestMethod.POST})
    protected GetWorldMarketHotList getWorldMarketHotList(
            @PathVariable String region,
            @RequestParam(required = false) String lang
    ) throws AbstractException {
        var key = noArgumentRequest(region, MarketEndpoint.MARKET_HOT_LIST);
        return marketService.requestParsedItem(key, lang);
    }

    @GetMapping(path = {"/GetWorldMarketList", "/category"})
    protected GetWorldMarketList getWorldMarketList(
            @PathVariable String region,
            @RequestParam Long mainCategory,
            @RequestParam(required = false) Long subCategory,
            @RequestParam(required = false) String lang
    ) throws AbstractException {
        var category = new Category(mainCategory, subCategory);
        var key = categoryRequest(category, region);
        return marketService.requestParsedItems(key, lang);
    }

    @PostMapping(path = {"/GetWorldMarketList", "/category"})
    protected GetWorldMarketList getWorldMarketList(
            @PathVariable String region,
            @RequestBody Category category,
            @RequestParam(required = false) String lang) throws AbstractException {
        var key = categoryRequest(category, region);
        return marketService.requestParsedItems(key, lang);
    }

    @GetMapping(path = {"/GetWorldMarketSearchList", "/search"})
    protected GetWorldMarketSearchList getWorldMarketSearchList(
            @PathVariable String region,
            @RequestParam Set<Long> ids,
            @RequestParam(required = false) String lang
    ) throws AbstractException {
        var key = searchRequest(ids, region);
        return marketService.requestParsedItem(key, lang);
    }

    @PostMapping(path = {"/GetWorldMarketSearchList", "/search"})
    protected GetWorldMarketSearchList getWorldMarketSearchList(
            @PathVariable String region,
            @RequestBody Ids ids,
            @RequestParam(required = false) String lang
    ) throws AbstractException {
        var key = searchRequest(ids, region);
        return marketService.requestParsedItem(key, lang);
    }

    @RequestMapping(value = "/market", method = {RequestMethod.GET, RequestMethod.POST})
    protected GetWorldMarketList getMarket(
            @PathVariable String region,
            @RequestParam(required = false) String lang
    ) throws AbstractException {
        var categories = categoryService.getMainCategories().stream().map(Category::new).toList();
        var keys = new ArrayList<CacheCompositeKey>();
        for (var category : categories) {
            keys.addAll(categoryRequest(category, region));
        }

        return marketService.requestParsedItems(keys, lang);
    }

    @RequestMapping(value = "/pearlItems", method = {RequestMethod.GET, RequestMethod.POST})
    protected GetWorldMarketList getPearlItems(
            @PathVariable String region,
            @RequestParam(required = false) String lang
    ) throws AbstractException {
        var pearlItemsCategory = new Category(55L);
        var keys = categoryRequest(pearlItemsCategory, region);
        return marketService.requestParsedItems(keys, lang);
    }
}
