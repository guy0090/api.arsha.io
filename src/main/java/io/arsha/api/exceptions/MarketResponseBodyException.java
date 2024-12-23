package io.arsha.api.exceptions;

public class MarketResponseBodyException extends AbstractException {

    public MarketResponseBodyException() {
        super(500, ExceptionCode.INVALID_MARKET_RESPONSE_BODY,
            "One or more requests returned invalid data (probably blocked by Imperva). Try again later.");
    }
}
