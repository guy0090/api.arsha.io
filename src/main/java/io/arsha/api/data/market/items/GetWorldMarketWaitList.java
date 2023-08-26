package io.arsha.api.data.market.items;

import io.arsha.api.data.CacheCompositeKey;
import io.arsha.api.data.market.MarketResponse;
import io.arsha.api.data.market.responses.ParsedItems;
import io.arsha.api.data.market.responses.ParsedList;
import lombok.Data;
import lombok.EqualsAndHashCode;

public class GetWorldMarketWaitList extends ParsedItems<ParsedList> {

    @Override
    public void addResponse(String name, CacheCompositeKey key, MarketResponse response) {
        var results = new ParsedList();
        if (response.hasBadData()) {
            add(results);
            return;
        }

        var details = response.getResult();
        details.forEach(item -> results.add(new Item(name, item)));
        add(results);
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Item extends ParsedList.ParsedItem
    {
        Long price;
        Long liveAt;

        Item(String name, String item) {
            String[] attributes = item.split("-");

            setName(name);
            setId(Long.valueOf(attributes[0]));
            setSid(Long.valueOf(attributes[1]));
            price = Long.valueOf(attributes[2]);
            liveAt = Long.valueOf(attributes[3]);
        }
    }
}