package io.arsha.api.services;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

@Service
public class WebSocketSessionService {

    private final Map<String, SessionHolder> sessions = new ConcurrentHashMap<>();

    public void addSession(WebSocketSession session) {
        var id = session.getId();
        sessions.put(id, new SessionHolder(session, LocalDateTime.now()));
    }

    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
    }

    public Set<WebSocketSession> getSessions() {
        return sessions.values().stream().map(SessionHolder::session).collect(Collectors.toSet());
    }

    public record SessionHolder(WebSocketSession session, LocalDateTime added) {

    }
}
