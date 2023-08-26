package io.arsha.api.data.market.responses;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.arsha.api.data.CacheCompositeKey;
import io.arsha.api.data.market.MarketResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Convenience class for creating a list of {@link MarketResponse} objects.
 * <br>
 * "Raw" in this context is supposed to mean unparsed responses from the official API.
 */
@JsonSerialize(using = RawItems.Serializer.class)
public class RawItems extends MarketResponses<MarketResponse> {

    public RawItems(MarketResponse item) {
        super(item);
    }

    public RawItems(List<MarketResponse> results) {
        super(results);
    }

    public static RawItems fromResults(Map<CacheCompositeKey, MarketResponse> results) {
        return new RawItems(results.values().stream().toList());
    }

    public RawItems combinedResult(String delimiter) {
        var resultMsg = stream()
                .map(MarketResponse::getResultMessage)
                .collect(Collectors.joining(delimiter));

        clear();
        add(new MarketResponse(resultMsg));
        return this;
    }

    protected static class Serializer extends JsonSerializer<RawItems> {
        @Override
        public void serialize(RawItems values, JsonGenerator gen, SerializerProvider provider) throws IOException {
            if (values.size() == 1) {
                values.get(0).trimDelimiter();
                gen.writeObject(values.get(0));
            } else {
                gen.writeStartArray();
                for (var item : values) {
                    item.trimDelimiter();
                    gen.writeObject(item);
                }
                gen.writeEndArray();
            }
        }
    }
}
