package io.arsha.api.data.market.items;

import io.arsha.api.data.CacheCompositeKey;
import io.arsha.api.data.market.MarketResponse;
import io.arsha.api.data.market.responses.ParsedItems;
import io.arsha.api.data.market.responses.ParsedList;
import lombok.Data;
import lombok.EqualsAndHashCode;

public class GetWorldMarketSubList extends ParsedItems<ParsedList> {

    @Override
    public void addResponse(String name, CacheCompositeKey key, MarketResponse response) {
        var results = new ParsedList();
        var details = response.getResult();

        details.forEach(item -> results.add(new Item(name, item)));
        add(results);
    }

    public Item getItem(Long id, Long subId) {
        return (Item) stream()
                .flatMap(ParsedList::stream)
                .filter(i -> i.getId().equals(id) && i.getSid().equals(subId))
                .findFirst()
                .orElse(null);
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Item extends ParsedList.ParsedItem {
        Long minEnhance;
        Long maxEnhance;
        Long basePrice;
        Long currentStock;
        Long totalTrades;
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
            priceMin = Long.valueOf(attributes[6]);
            priceMax = Long.valueOf(attributes[7]);
            lastSoldPrice = Long.valueOf(attributes[8]);
            lastSoldTime = Long.valueOf(attributes[9]);
        }
    }
}