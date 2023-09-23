package io.arsha.api.data.market.items;

import io.arsha.api.data.CacheCompositeKey;
import io.arsha.api.data.market.MarketResponse;
import io.arsha.api.data.market.responses.ParsedItems;
import io.arsha.api.data.market.responses.ParsedList;
import io.arsha.api.util.DateUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GetMarketPriceInfo extends ParsedItems<ParsedList> {

    @Override
    public void addResponse(String name, CacheCompositeKey key, MarketResponse response) {
        var item = new Item(name, key, response.getResultMessage());
        var results = new ParsedList(item);
        add(results);
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Item extends ParsedList.ParsedItem
    {
        Map<Long, Long> history = new LinkedHashMap<>();

        Item(String name, CacheCompositeKey key, String item) {
            List<Long> prices = Arrays.stream(item.split("-"))
                    .map(Long::valueOf)
                    .toList();

            setName(name);
            setId(key.getPrimary());
            setSid(key.getSecondary());

            long ninetyDaysAgo = DateUtils.startOfDay(prices.size());
            long oneDayInMillis = 86400000L;

            for (var i = 0; i < prices.size(); i++) {
                var time = ninetyDaysAgo + (oneDayInMillis * i);
                var price = prices.get(i);
                history.put(time, price);
            }
        }
    }
}