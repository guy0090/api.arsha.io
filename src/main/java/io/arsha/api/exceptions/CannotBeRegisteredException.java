package io.arsha.api.exceptions;

import io.arsha.api.data.market.MarketResponse;
import lombok.Getter;

public class CannotBeRegisteredException extends AbstractException {

    @Getter
    private final MarketResponse response;

    public CannotBeRegisteredException(MarketResponse response) {
        super();
        this.response = response;
    }

}
