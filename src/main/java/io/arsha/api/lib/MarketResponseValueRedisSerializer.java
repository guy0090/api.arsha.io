package io.arsha.api.lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.arsha.api.data.market.MarketResponse;
import lombok.NoArgsConstructor;
import org.apache.tomcat.util.buf.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.IOException;

@NoArgsConstructor
public class MarketResponseValueRedisSerializer implements RedisSerializer<MarketResponse> {

    private final Logger logger = LoggerFactory.getLogger(MarketResponseValueRedisSerializer.class);
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public byte[] serialize(MarketResponse value) throws SerializationException {
        if (value == null) {
            return null;
        }

        try {
            return mapper.writeValueAsBytes(value);
        } catch (IOException e) {
            logger.error("Failed to serialize market response {}", value, e);
            return null;
        }
    }

    @Override
    public MarketResponse deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null) {
            return null;
        }

        try {
            return mapper.readValue(bytes, MarketResponse.class);
        } catch (IOException e) {
            logger.error("Failed to deserialize market response value {}", HexUtils.toHexString(bytes), e);
            return null;
        }
    }
}
