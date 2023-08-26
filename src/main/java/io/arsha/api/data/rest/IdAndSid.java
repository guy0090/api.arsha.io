package io.arsha.api.data.rest;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;

public class IdAndSid extends LinkedHashSet<IdAndSid.Item> {

    public static IdAndSid singleton(Long id, @Nullable Long sid) {
        var subId = sid == null ? 0L : sid;
        var item = Item.builder().id(id).sid(subId).build();
        var list = new IdAndSid();
        list.add(item);

        return list;
    }

    public static IdAndSid collect(@NonNull List<Long> id, @Nullable List<Long> sid) {
        var list = new IdAndSid();
        for (var i = 0; i < id.size(); i++) {
            var subId = sid == null ? 0L : sid.get(i);
            var item = Item.builder().id(id.get(i)).sid(subId).build();
            list.add(item);
        }

        return list;
    }

    @Getter
    @Validated
    @JsonDeserialize(using = Item.Deserializer.class)
    @Builder(toBuilder = true)
    @EqualsAndHashCode
    public static class Item {
        @NotNull
        Long id;
        @Nullable @Min(0) @Max(20)
        Long sid;

        protected static class Deserializer extends JsonDeserializer<Item> {

            public Deserializer() {
                super();
            }

            @Override
            public Item deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
                var node = (JsonNode) p.getCodec().readTree(p);
                var id = node.get("id").asLong();
                var sidNode = node.get("sid");
                var sid = sidNode == null ? 0L : sidNode.asLong();

                return Item.builder().id(id).sid(sid).build();
            }
        }
    }
}
