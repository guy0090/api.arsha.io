package io.arsha.api.data.websocket;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.context.ApplicationEvent;

@JsonIgnoreProperties("source")
public abstract class WebSocketEvent extends ApplicationEvent {

    protected WebSocketEvent() {
        super("WebSocket");
    }

    @JsonProperty("type")
    public abstract String getType();
}
