package io.arsha.api.exceptions;

import io.arsha.api.data.market.MarketResponse;
import lombok.Getter;

public class CannotBeRegisteredException extends AbstractException {

    @Getter
    private final MarketResponse response;

    public CannotBeRegisteredException(String response) {
        super();
        this.response = MarketResponse.deserialize(response);
    }

}
