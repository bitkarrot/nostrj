package org.nostrj.relay.websocket;

import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import org.nostrj.server.RelayHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerWebSocket("/")
public class NostrWebSocketServer {
    private static final Logger log = LoggerFactory.getLogger(NostrWebSocketServer.class);
    
    private final RelayHandler relayHandler;
    private final Map<String, WebSocketSession> sessions;

    public NostrWebSocketServer(RelayHandler relayHandler) {
        this.relayHandler = relayHandler;
        this.sessions = new ConcurrentHashMap<>();
    }

    @OnOpen
    public void onOpen(WebSocketSession session) {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        log.info("WebSocket connection opened: {}", sessionId);
    }

    @OnMessage
    public void onMessage(String message, WebSocketSession session) {
        String sessionId = session.getId();
        log.debug("Received message from {}: {}", sessionId, message);
        
        try {
            String response = relayHandler.handleMessage(sessionId, message);
            if (response != null && !response.isEmpty()) {
                String[] responses = response.split("\n");
                for (String resp : responses) {
                    if (!resp.trim().isEmpty()) {
                        session.sendSync(resp);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error handling message from session {}", sessionId, e);
            try {
                session.sendSync("[\"NOTICE\",\"Error processing message: " + e.getMessage() + "\"]");
            } catch (Exception ex) {
                log.error("Failed to send error notice", ex);
            }
        }
    }

    @OnClose
    public void onClose(WebSocketSession session) {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        relayHandler.removeSession(sessionId);
        log.info("WebSocket connection closed: {}", sessionId);
    }
}
