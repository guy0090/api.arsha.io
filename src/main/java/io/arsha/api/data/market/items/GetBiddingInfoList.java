package io.arsha.api.data.market.items;

import io.arsha.api.data.CacheCompositeKey;
import io.arsha.api.data.market.MarketResponse;
import io.arsha.api.data.market.responses.ParsedItems;
import io.arsha.api.data.market.responses.ParsedList;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GetBiddingInfoList extends ParsedItems<ParsedList> {

    @Override
    public void addResponse(String name, CacheCompositeKey key, MarketResponse response) {
        var item = new Item(name, key, response.getResult());
        var results = new ParsedList(item);
        add(results);
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Item extends ParsedList.ParsedItem
    {
        List<Map<String, Long>> orders = new ArrayList<>();

        Item(String name, CacheCompositeKey key, List<String> items) {
            items.forEach(item -> {
                String[] attributes = item.split("-");
                var map = new LinkedHashMap<String, Long>();
                map.put("price", Long.valueOf(attributes[0]));
                map.put("buyers", Long.valueOf(attributes[1]));
                map.put("sellers", Long.valueOf(attributes[2]));
                orders.add(map);
            });

            setName(name);
            setId(key.getPrimary());
            setSid(key.getSecondary());
        }
    }
}