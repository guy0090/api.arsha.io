package io.arsha.api.data.websocket;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.arsha.api.data.CacheCompositeKey;
import java.util.Optional;
import lombok.Getter;

/**
 * Event signaling that a key in Redis has expired.
 */
@Getter
public class ExpiredEvent extends WebSocketEvent {

    private final String type = "expired";

    @JsonInclude(Include.NON_NULL)
    private final String id;
    @JsonInclude(Include.NON_NULL)
    private final String sid;
    @JsonInclude(Include.NON_NULL)
    private final String search;
    private final String region;
    private final String endpoint;

    public ExpiredEvent(CacheCompositeKey key) {
        this.id = Optional.ofNullable(key.getPrimary()).map(Object::toString).orElse(null);
        this.sid = Optional.ofNullable(key.getSecondary()).map(Object::toString).orElse(null);
        this.search = Optional.ofNullable(key.getSearch()).map(Object::toString).orElse(null);
        this.region = key.getRegion().name();
        this.endpoint = key.getEndpoint().getPath();
    }
}
