package io.arsha.api.data.market.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public abstract class MarketRequestBody {

    @JsonProperty
    private Integer keyType = 0;
}
