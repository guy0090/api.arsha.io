package io.arsha.api.exceptions;

import io.arsha.api.data.CacheCompositeKey;
import io.arsha.api.data.rest.Category;

public class MarketRequestException extends AbstractException {

    public MarketRequestException() {
        super(500, ExceptionCode.FAILED_MARKET_REQUEST, "An unexpected error occurred while processing your request to the market API");
    }

    public MarketRequestException(String message) {
        super(500, ExceptionCode.FAILED_MARKET_REQUEST, message);
    }

    public MarketRequestException(Long itemId) {
        super(404, ExceptionCode.FAILED_MARKET_REQUEST);
        this.message = String.format("The request with provided ID '%s' failed", itemId);
    }

    public MarketRequestException(CacheCompositeKey key) {
        super(404, ExceptionCode.FAILED_MARKET_REQUEST);
        var region = key.getRegion().getRegion();
        this.message = switch (key.getEndpoint()) {
            case MARKET_SEARCH_LIST -> {
                var ids = key.getSearch();
                yield String.format("Some of the IDs provided do not have data in this region (%s): '%s'", region, ids);
            }
            case MARKET_LIST -> {
                var mainCategory = key.getPrimary();
                var subCategory = key.getSecondary();
                yield String.format("The category provided does not exist. mainCategory: %s, subCategory: %s", mainCategory, subCategory);
            }
            default -> {
                var itemId = key.getPrimary();
                var subItemId = key.getSecondary();
                yield String.format("The item provided does not exist or has no data (%s): '%s'/'%s'", region, itemId, subItemId);
            }
        };
    }

    public MarketRequestException(Category category) {
        super(404, ExceptionCode.FAILED_MARKET_REQUEST);
        this.message = String.format("The category combination provided does not exist: %s:%s",
                category.getMainCategory(), category.getSubCategory());
    }
}
