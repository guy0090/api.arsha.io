package io.arsha.api.data.market;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * Default shape of a response from official market API.
 */
@Data
@Jacksonized
@AllArgsConstructor
@Builder(toBuilder = true)
public class MarketResponse implements IMarketResponse {

    @Builder.Default
    @JsonProperty("resultCode")
    private Integer resultCode = 0;

    @JsonProperty("resultMsg")
    private String resultMessage;

    public MarketResponse(String resultMessage) {
        this.resultMessage = resultMessage;
        this.resultCode = 0;
    }

    public static MarketResponse deserialize(String json) {
        var mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, MarketResponse.class);
        } catch (Exception e) {
            return new MarketResponse("0");
        }
    }

    public void trimDelimiter() {
        if (resultMessage.endsWith("|")) resultMessage = resultMessage.substring(0, resultMessage.length() - 1);
    }

    @JsonIgnore
    public boolean hasBadData() {
        return resultMessage == null || resultMessage.equals("0");
    }

    @JsonIgnore
    public List<String> getResult() {
        return switch (resultMessage) {
            case "" -> List.of();
            case String msg -> {
                if (msg.endsWith("|")) msg = msg.substring(0, msg.length() - 1);
                yield List.of(msg.split("[|]"));
            }
        };
    }

}