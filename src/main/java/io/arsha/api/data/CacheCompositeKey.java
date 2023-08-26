package io.arsha.api.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.arsha.api.data.market.common.GameRegion;
import io.arsha.api.data.market.common.MarketEndpoint;
import jakarta.annotation.Nullable;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

@Getter
@ToString
@Jacksonized
@EqualsAndHashCode
@Builder(toBuilder = true)
@JsonSerialize(using = CacheCompositeKey.Serializer.class)
public class CacheCompositeKey {
    @Nullable String search; // Search query
    @Nullable Long primary; // Alias for item or category ID
    @Nullable Long secondary; // Alias for item sub ID or sub-category ID
    @NonNull GameRegion region;
    @NonNull MarketEndpoint endpoint;

    @SneakyThrows
    public byte[] serialize() {
        var mapper = new ObjectMapper();
        return mapper.writeValueAsBytes(this);
    }

    public static CacheCompositeKey createCombinedSearchKey(List<CacheCompositeKey> keys) {
        if (keys.stream().anyMatch(k -> k.getSearch() == null)) {
            throw new IllegalArgumentException("All keys must have a search query");
        }

        return CacheCompositeKey.builder()
                .search(String.join(",", keys.stream().map(CacheCompositeKey::getSearch).toList()))
                .region(keys.get(0).getRegion())
                .endpoint(MarketEndpoint.MARKET_SEARCH_LIST)
                .build();
    }

    @JsonIgnore
    public List<Long> getSearchIds() {
        if (search == null) return List.of();
        return Stream.of(search.split(",")).map(Long::valueOf).toList();
    }

    public List<Long> getPrimaryIds() {
        if (primary == null) return List.of();
        if (search != null) return getSearchIds();
        return List.of(primary);
    }

    protected static class Serializer extends JsonSerializer<CacheCompositeKey> {
        @Override
        public void serialize(CacheCompositeKey value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();

            if (value.getSearch() != null) {
                gen.writeStringField("search", value.getSearch());
            } else if (value.getPrimary() != null) {
                gen.writeNumberField("primary", value.getPrimary());
                if (value.getSecondary() != null) {
                    gen.writeNumberField("secondary", value.getSecondary());
                }
            }

            gen.writeStringField("region", value.getRegion().toString());
            gen.writeStringField("endpoint", value.getEndpoint().toString());
            gen.writeEndObject();
        }
    }

}
