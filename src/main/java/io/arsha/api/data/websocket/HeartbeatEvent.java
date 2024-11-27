package io.arsha.api.data.websocket;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties("timestamp")
public class HeartbeatEvent extends WebSocketEvent {
    private final String type = "heartbeat";
    private final String host;

    public HeartbeatEvent(String host) {
        this.host = host;
    }
}
