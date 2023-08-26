package io.arsha.api.data.market.items;

import io.arsha.api.data.CacheCompositeKey;
import io.arsha.api.data.market.MarketResponse;
import io.arsha.api.data.market.responses.ParsedItems;
import io.arsha.api.data.market.responses.ParsedList;
import lombok.Data;
import lombok.EqualsAndHashCode;

public class GetWorldMarketSearchList extends ParsedItems<ParsedList> {

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
        Long currentStock;
        Long totalTrades;
        Long basePrice;

        Item(String name, String item) {
            String[] attributes = item.split("-");

            setName(name);
            setId(Long.valueOf(attributes[0]));
            currentStock = Long.valueOf(attributes[1]);
            totalTrades = Long.valueOf(attributes[2]);
            basePrice = Long.valueOf(attributes[3]);
        }
    }
}