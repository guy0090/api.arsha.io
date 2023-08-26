package io.arsha.api.controllers;

import io.arsha.api.data.market.common.MarketEndpoint;
import io.arsha.api.data.market.items.GetWorldMarketSubList;
import io.arsha.api.data.rest.Category;
import io.arsha.api.data.rest.IdAndSid;
import io.arsha.api.data.rest.Ids;
import io.arsha.api.data.market.MarketResponse;
import io.arsha.api.data.market.items.Price;
import io.arsha.api.data.market.responses.RawItems;
import io.arsha.api.exceptions.AbstractException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Slf4j
@Validated
@RestController
@RequestMapping("/v1/{region}")
public class V1Controller extends AbstractController<MarketResponse> {

    @GetMapping(path = {"/GetWorldMarketSubList", "/item"})
    protected RawItems getWorldMarketSubList(
            @PathVariable String region,
            @RequestParam Set<Long> id,
            @RequestParam(required = false) String lang
    ) throws AbstractException {
        var endpoint = MarketEndpoint.MARKET_SUB_LIST;
        var gameRegion = validateRegion(region);

        var keys = createKeys(id.stream().toList(), gameRegion, endpoint);
        return marketService.requestMany(keys);
    }

    @PostMapping(path = {"/GetWorldMarketSubList", "/item"})
    protected RawItems getWorldMarketSubList(
            @PathVariable String region,
            @RequestBody Ids ids,
            @RequestParam(required = false) String lang
    ) throws AbstractException {
        var endpoint = MarketEndpoint.MARKET_SUB_LIST;
        var gameRegion = validateRegion(region);

        var keys = createKeys(ids.stream().toList(), gameRegion, endpoint);
        return marketService.requestMany(keys);
    }

    @GetMapping(path = {"/GetMarketPriceInfo", "/history"})
    protected RawItems getMarketPriceInfo(
            @PathVariable String region,
            @RequestParam List<Long> id,
            @RequestParam(required = false) List<Long> sid,
            @RequestParam(required = false) String lang
    ) throws AbstractException {
        var keys = idAndSidRequest(region, id, sid, MarketEndpoint.MARKET_PRICE_INFO);
        return marketService.requestMany(keys);
    }

    @PostMapping(path = {"/GetMarketPriceInfo", "/history"})
    protected RawItems getMarketPriceInfo(
            @PathVariable String region,
            @RequestBody IdAndSid body,
            @RequestParam(required = false) String lang
    ) throws AbstractException {
        var keys = idAndSidRequest(region, body, MarketEndpoint.MARKET_PRICE_INFO);
        return marketService.requestMany(keys);
    }

    @GetMapping(path = {"/GetBiddingInfoList", "/orders"})
    protected RawItems getBiddingInfoList(
            @PathVariable String region,
            @RequestParam List<Long> id,
            @RequestParam(required = false) List<Long> sid,
            @RequestParam(required = false) String lang
    ) throws AbstractException {
        var keys = idAndSidRequest(region, id, sid, MarketEndpoint.BIDDING_INFO_LIST);
        return marketService.requestMany(keys);
    }

    @PostMapping(path = {"/GetBiddingInfoList", "/orders"})
    protected RawItems getBiddingInfoList(
            @PathVariable String region,
            @RequestBody IdAndSid body,
            @RequestParam(required = false) String lang
    ) throws AbstractException {
        var keys = idAndSidRequest(region, body, MarketEndpoint.BIDDING_INFO_LIST);
        return marketService.requestMany(keys);
    }

    @RequestMapping(value = {"/GetWorldMarketWaitList", "/queue"}, method = {RequestMethod.GET, RequestMethod.POST})
    protected RawItems getWorldMarketWaitList(
            @PathVariable String region,
            @RequestParam(required = false) String lang
    ) throws AbstractException {
        var key = noArgumentRequest(region, MarketEndpoint.MARKET_WAIT_LIST);
        return marketService.requestOne(key);
    }

    @RequestMapping(value = {"/GetWorldMarketHotList", "/hot"}, method = {RequestMethod.GET, RequestMethod.POST})
    protected RawItems getWorldMarketHotList(
            @PathVariable String region,
            @RequestParam(required = false) String lang
    ) throws AbstractException {
        var key = noArgumentRequest(region, MarketEndpoint.MARKET_HOT_LIST);
        return marketService.requestOne(key);
    }

    @GetMapping(value = {"/GetWorldMarketList", "/category"})
    protected RawItems getWorldMarketList(
            @PathVariable String region,
            @RequestParam Long mainCategory,
            @RequestParam(required = false) Long subCategory,
            @RequestParam(required = false) String lang
    ) throws AbstractException {
        var category = new Category(mainCategory, subCategory);
        var keys = categoryRequest(category, region);
        return marketService.requestCategoryResult(keys);
    }

    @PostMapping(value = {"/GetWorldMarketList", "/category"})
    protected RawItems getWorldMarketList(
            @PathVariable String region,
            @RequestBody Category categories,
            @RequestParam(required = false) String lang
    ) throws AbstractException {
        var keys = categoryRequest(categories, region);
        return marketService.requestCategoryResult(keys);
    }

    @GetMapping(value = {"/GetWorldMarketSearchList", "/search"})
    protected RawItems getWorldMarketSearchList(
            @PathVariable String region,
            @RequestParam Set<Long> ids,
            @RequestParam(required = false) String lang
    ) throws AbstractException {
        var key = searchRequest(ids, region);
        return marketService.requestSearchResult(key);
    }

    @PostMapping(value = {"/GetWorldMarketSearchList", "/search"})
    protected RawItems getWorldMarketSearchList(
            @PathVariable String region,
            @RequestBody Ids ids,
            @RequestParam(required = false) String lang
    ) throws AbstractException {
        var key = searchRequest(ids, region);
        return marketService.requestSearchResult(key);
    }

    /// Legacy convenience method

    @GetMapping("/price")
    protected Price getPriceItem(
            @PathVariable String region,
            @RequestParam Long id,
            @RequestParam(required = false) Long sid,
            @RequestParam(required = false) String lang
    ) throws AbstractException {
        var idaAndSid = IdAndSid.singleton(id, sid);
        return priceItemRequest(region, idaAndSid, lang);
    }

    @PostMapping("/price")
    protected Price getPriceItem(
            @PathVariable String region,
            @RequestBody IdAndSid body,
            @RequestParam(required = false) String lang
    ) throws AbstractException {
        return priceItemRequest(region, body, lang);
    }

    protected Price priceItemRequest(String region, IdAndSid body, String lang) throws AbstractException {
        var keys = idAndSidRequest(region, body, MarketEndpoint.MARKET_SUB_LIST);
        GetWorldMarketSubList responses = marketService.requestParsedItems(keys, lang);
        return new Price(keys, responses);
    }
}
