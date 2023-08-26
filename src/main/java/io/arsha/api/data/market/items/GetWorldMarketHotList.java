package io.arsha.api.data.market.items;

import io.arsha.api.data.CacheCompositeKey;
import io.arsha.api.data.market.MarketResponse;
import io.arsha.api.data.market.responses.ParsedItems;
import io.arsha.api.data.market.responses.ParsedList;
import lombok.Data;
import lombok.EqualsAndHashCode;

public class GetWorldMarketHotList extends ParsedItems<ParsedList> {

    @Override
    public void addResponse(String name, CacheCompositeKey key, MarketResponse response) {
        var results = new ParsedList();
        var details = response.getResult();

        details.forEach(item -> results.add(new Item(name, item)));
        add(results);
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Item extends ParsedList.ParsedItem {
        Long minEnhance;
        Long maxEnhance;
        Long basePrice;
        Long currentStock;
        Long totalTrades;
        Long priceChangeDirection;
        Long priceChangeValue;
        Long priceMin;
        Long priceMax;
        Long lastSoldPrice;
        Long lastSoldTime;

        Item(String name, String item) {
            String[] attributes = item.split("-");

            setName(name);
            setId(Long.valueOf(attributes[0]));
            setSid(Long.valueOf(attributes[1]));
            minEnhance = Long.valueOf(attributes[1]);
            maxEnhance = Long.valueOf(attributes[2]);
            basePrice = Long.valueOf(attributes[3]);
            currentStock = Long.valueOf(attributes[4]);
            totalTrades = Long.valueOf(attributes[5]);
            priceChangeDirection = Long.valueOf(attributes[6]);
            priceChangeValue = Long.valueOf(attributes[7]);
            priceMin = Long.valueOf(attributes[8]);
            priceMax = Long.valueOf(attributes[9]);
            lastSoldPrice = Long.valueOf(attributes[10]);
            lastSoldTime = Long.valueOf(attributes[11]);
        }
    }
}