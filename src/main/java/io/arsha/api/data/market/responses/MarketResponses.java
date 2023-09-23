package io.arsha.api.data.market.responses;

import io.arsha.api.data.market.IMarketResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to handle serializing a single item or a list of items.
 * <br>
 * Will serialize as a single item if there is only one item in the list instead
 * of as an array.
 */
public abstract class MarketResponses<T extends IMarketResponse> extends ArrayList<T> {

    protected MarketResponses() {
        super();
    }

    protected MarketResponses(T item) {
        super(1);
        add(item);
    }

    protected MarketResponses(List<T> results) {
        super(results.size());
        addAll(results);
    }

}
