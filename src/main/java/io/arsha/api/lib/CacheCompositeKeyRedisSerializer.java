package io.arsha.api.lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.arsha.api.data.CacheCompositeKey;
import lombok.NoArgsConstructor;
import org.apache.tomcat.util.buf.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.IOException;

@NoArgsConstructor
public class CacheCompositeKeyRedisSerializer implements RedisSerializer<CacheCompositeKey> {

    private final Logger logger = LoggerFactory.getLogger(CacheCompositeKeyRedisSerializer.class);
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public byte[] serialize(CacheCompositeKey key) throws SerializationException {
        if (key == null) {
            return null;
        }

        return key.serialize();
    }

    @Override
    public CacheCompositeKey deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null) {
            return null;
        }

        try {
            return mapper.readValue(bytes, CacheCompositeKey.class);
        } catch (IOException e) {
            logger.error("Failed to deserialize cache composite key {}", HexUtils.toHexString(bytes), e);
            return null;
        }
    }
}
