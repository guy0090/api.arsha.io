package io.arsha.api.data.market.responses;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.arsha.api.data.CacheCompositeKey;
import io.arsha.api.data.market.MarketResponse;
import io.arsha.api.data.market.common.MarketEndpoint;
import io.arsha.api.data.scraper.ScrapedItem;
import io.arsha.api.exceptions.InvalidParsingClassException;
import lombok.EqualsAndHashCode;

import java.io.IOException;
import java.util.*;

@EqualsAndHashCode(callSuper = true)
@JsonSerialize(using = ParsedItems.Serializer.class)
public abstract class ParsedItems<T extends ParsedList> extends MarketResponses<T> {

    protected static final String PACKAGE_NAME = "io.arsha.api.data.market.items";

    public abstract void addResponse(String name, CacheCompositeKey key, MarketResponse response);

    public List<Long> getItemIds() {
        var ids = new ArrayList<Long>();
        forEach(items -> items.forEach(item -> ids.add(item.getId())));
        return ids;
    }

    public void setNames(Map<Long, ScrapedItem> data) {
        forEach(items -> items.forEach(item -> {
            var opt = Optional.ofNullable(data.get(item.getId()));
            opt.ifPresent(dat -> item.setName(dat.getName()));
        }));
    }

    @SuppressWarnings("unchecked")
    public static <R extends ParsedItems<?>> R getParsingClass(MarketEndpoint endpoint)
            throws InvalidParsingClassException {
        try {
            var className = String.format("%s.%s", PACKAGE_NAME, endpoint.getPath());

            var clazz = Class.forName(className);
            var constructor = Arrays.stream(clazz.getConstructors())
                    .filter(c -> c.getParameterCount() == 0)
                    .findFirst()
                    .orElseThrow();

            return (R) constructor.newInstance();
        } catch (Exception e) {
            throw new InvalidParsingClassException(e.getMessage());
        }
    }

    protected static class Serializer extends JsonSerializer<ParsedItems<ParsedList>> {
        @Override
        public void serialize(ParsedItems<ParsedList> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            var values = value.stream().toList();
            if (values.isEmpty()) {
                gen.writeStartArray();
                gen.writeEndArray();
                return;
            }

            var isSingleton = values.size() == 1 && values.getFirst().size() == 1;
            var onlySingletons = values.stream().allMatch(list -> list.size() == 1);

            if (isSingleton) {
                gen.writeObject(values.getFirst().getFirst());
            } else if (values.size() == 1) {
                gen.writeStartArray();
                for (var item : values.getFirst()) {
                    gen.writeObject(item);
                }
                gen.writeEndArray();
            } else if (onlySingletons) {
                gen.writeStartArray();
                for (var item : values) {
                    gen.writeObject(item.getFirst());
                }
                gen.writeEndArray();
            } else {
                gen.writeStartArray();

                for (var item : values) {
                    gen.writeStartArray();
                    for (var subItem : item) {
                        gen.writeObject(subItem);
                    }
                    gen.writeEndArray();
                }
                gen.writeEndArray();
            }
        }
    }
}
