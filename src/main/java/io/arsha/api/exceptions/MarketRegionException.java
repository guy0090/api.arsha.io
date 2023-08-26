package io.arsha.api.exceptions;

import io.arsha.api.data.market.common.GameRegion;

public class MarketRegionException extends AbstractException {

    public MarketRegionException() {
        super(400, ExceptionCode.INVALID_REGION);
        this.message = String.format("Invalid region. Valid regions are: %s", GameRegion.REGIONS);
    }

    public MarketRegionException(String region) {
        super(400, ExceptionCode.INVALID_REGION);
        this.message = String.format("Invalid region '%s'. Valid regions are: %s", region, GameRegion.REGIONS);
    }

    public MarketRegionException(GameRegion region) {
        super(400, ExceptionCode.DISABLED_REGION);
        this.message = String.format("Region '%s' is disabled", region.name());
    }

}
