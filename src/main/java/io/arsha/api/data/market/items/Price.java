package io.arsha.api.data.market.items;

import io.arsha.api.data.CacheCompositeKey;
import io.arsha.api.data.market.MarketResponse;
import io.arsha.api.data.market.responses.ParsedItems;
import io.arsha.api.data.market.responses.ParsedList;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

public class Price extends ParsedItems<ParsedList> {

    @Override
    public void addResponse(String name, CacheCompositeKey key, MarketResponse response) {
        // Unused
    }

    public Price(List<CacheCompositeKey> keys, GetWorldMarketSubList source) {
        var result = new ParsedList();
        for (var key : keys) {
            var item = source.getItem(key.getPrimary(), key.getSecondary());
            if (item != null) {
                result.add(new Item(item));
            }
        }
        add(result);
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Item extends ParsedList.ParsedItem {
        Long basePrice;

        public Item(GetWorldMarketSubList.Item source) {
            setName(source.getName());
            setId(source.getId());
            setSid(source.getSid());
            basePrice = source.getBasePrice();
        }
    }
}
