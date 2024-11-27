package io.arsha.api.data.market.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MarketEndpoint {

    /**
     * Hot list items (items with volatile prices)
     */
    MARKET_HOT_LIST("GetWorldMarketHotList", true),

    /**
     * List of items by (sub)-category
     */
    MARKET_LIST("GetWorldMarketList", true),

    /**
     * An item with all possible enhancement levels
     */
    MARKET_SUB_LIST("GetWorldMarketSubList", false),

    /**
     * Items by search query
     */
    MARKET_SEARCH_LIST("GetWorldMarketSearchList", false),

    /**
     * Bidding information on an item (orders & sellers)
     */
    BIDDING_INFO_LIST("GetBiddingInfoList", true),

    /**
     * Historical price data for an item
     */
    MARKET_PRICE_INFO("GetMarketPriceInfo", false),

    /**
     * Items currently waiting to be sellable
     */
    MARKET_WAIT_LIST("GetWorldMarketWaitList", false);

    final String path;
    final boolean compressed;
}
