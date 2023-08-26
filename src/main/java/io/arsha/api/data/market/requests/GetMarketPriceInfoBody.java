package io.arsha.api.data.market.requests;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class GetMarketPriceInfoBody extends MarketRequestBody {
    Long mainKey;
    Long subKey;
}