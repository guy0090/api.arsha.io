package io.arsha.api.data.market.items;

import io.arsha.api.data.CacheCompositeKey;
import io.arsha.api.data.market.MarketResponse;
import io.arsha.api.data.market.responses.ParsedItems;
import io.arsha.api.data.market.responses.ParsedList;
import lombok.Data;
import lombok.EqualsAndHashCode;

public class GetWorldMarketList extends ParsedItems<ParsedList> {

    @Override
    public void addResponse(String name, CacheCompositeKey key, MarketResponse response) {
        var results = new ParsedList();
        var details = response.getResult();

        details.forEach(item -> results.add(new Item(name, key, item)));
        add(results);
    }

    public void combine() {
        var results = new ParsedList();

        for (var subList : this) {
            results.addAll(subList);
        }

        this.clear();
        this.add(results);
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Item extends ParsedList.ParsedItem {

        Long currentStock;
        Long totalTrades;
        Long basePrice;
        Long mainCategory;
        Long subCategory;

        Item(String name, CacheCompositeKey key, String item) {
            String[] attributes = item.split("-");

            setName(name);
            setId(Long.valueOf(attributes[0]));
            currentStock = Long.valueOf(attributes[1]);
            totalTrades = Long.valueOf(attributes[2]);
            basePrice = Long.valueOf(attributes[3]);
            mainCategory = key.getPrimary();
            subCategory = key.getSecondary();
        }
    }
}