package io.arsha.api.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.arsha.api.data.websocket.ExpiredEvent;
import io.arsha.api.data.websocket.HeartbeatEvent;
import io.arsha.api.services.WebSocketSessionService;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController extends TextWebSocketHandler implements
    ApplicationListener<ExpiredEvent> {

    private static final ObjectMapper mapper = new ObjectMapper();
    private final WebSocketSessionService wsSessionService;
    private final String hostName;

    @Scheduled(fixedRate = 30, timeUnit = TimeUnit.SECONDS)
    public synchronized void sendHeartbeat() {
        for (var session : wsSessionService.getSessions()) {
            publish(session, new HeartbeatEvent(hostName));
        }
    }

    @Override
    public synchronized void onApplicationEvent(@NonNull ExpiredEvent item) {
        for (var session : wsSessionService.getSessions()) {
            publish(session, item);
        }
    }

    private void publish(WebSocketSession session, Object event) {
        try {
            var serial = mapper.writeValueAsString(event);
            publish(session, serial);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event", e);
        }
    }

    private void publish(WebSocketSession session, String message) {
        try {
            session.sendMessage(new TextMessage(message));
        } catch (IOException e) {
            log.error("Failed to send message to session: {}", session.getId(), e);
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        // NOOP
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("Connection established: {}", session.getId());
        wsSessionService.addSession(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("Connection closed: {}", session.getId());
        wsSessionService.removeSession(session.getId());
    }

}
