package io.arsha.api.lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.arsha.api.data.scraper.ScrapedItem;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.buf.HexUtils;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.IOException;

@Slf4j
@NoArgsConstructor
public class ScrapedItemValueRedisSerializer implements RedisSerializer<ScrapedItem> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public byte[] serialize(ScrapedItem value) throws SerializationException {
        if (value == null) {
            return null;
        }

        try {
            return mapper.writeValueAsBytes(value);
        } catch (IOException e) {
            log.error("Failed to serialize market response {}", value, e);
            return null;
        }
    }

    @Override
    public ScrapedItem deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null) {
            return null;
        }

        try {
            return mapper.readValue(bytes, ScrapedItem.class);
        } catch (IOException e) {
            log.error("Failed to deserialize market response value {}", HexUtils.toHexString(bytes), e);
            return null;
        }
    }
}
