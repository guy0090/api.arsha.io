package io.arsha.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.arsha.api.data.CacheCompositeKey;
import io.arsha.api.data.websocket.ExpiredEvent;
import java.io.IOException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeyExpirationListener implements MessageListener {

    private static final ObjectMapper mapper = new ObjectMapper();

    private final ApplicationEventPublisher publisher;

    @Override
    public void onMessage(@NonNull Message message, byte[] pattern) {
        try {
            var cacheKey = mapper.readValue(message.getBody(), CacheCompositeKey.class);
            publisher.publishEvent(new ExpiredEvent(cacheKey));
        } catch (IOException e) {
            log.debug("Failed to deserialize to cache composite key", e);
        }
    }

}
