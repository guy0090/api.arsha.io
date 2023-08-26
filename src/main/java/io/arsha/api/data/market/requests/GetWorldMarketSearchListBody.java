package io.arsha.api.data.market.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Value
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GetWorldMarketSearchListBody extends MarketRequestBody {
    @JsonProperty
    String searchResult;

    public GetWorldMarketSearchListBody(List<Long> items) {
        this.searchResult = StringUtils.join(items, ",");
    }
}
