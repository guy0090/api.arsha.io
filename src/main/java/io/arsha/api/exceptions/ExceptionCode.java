package io.arsha.api.exceptions;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExceptionCode {

    GENERIC(-1),
    INVALID_PARSING_CLASS(0),

    // Regions
    INVALID_REGION(50),
    DISABLED_REGION(51),

    // Market Requests
    FAILED_MARKET_REQUEST(100),
    INVALID_MARKET_ITEM(101),
    INVALID_MARKET_CATEGORY(102),

    // Request Validation
    ID_SID_MISMATCH(200);

    private final int code;
}
