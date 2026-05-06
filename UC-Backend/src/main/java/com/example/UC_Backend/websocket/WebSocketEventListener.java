package com.example.UC_Backend.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tracks WebSocket session lifecycle events.
 * Useful for:
 *   - Counting active connections (observability)
 *   - Logging which agents/customers are online
 *   - Future: heartbeat detection for agent offline handling
 */
@Component
public class WebSocketEventListener {

    private static final Logger log = LoggerFactory.getLogger(WebSocketEventListener.class);

    /** Live session count — thread-safe */
    private final AtomicInteger activeConnections = new AtomicInteger(0);

    /** sessionId → destination (for debug) */
    private final ConcurrentHashMap<String, String> activeSessions = new ConcurrentHashMap<>();

    @EventListener
    public void handleConnect(SessionConnectedEvent event) {
        int count = activeConnections.incrementAndGet();
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        log.info("ws.connect sessionId={} total_active={}", accessor.getSessionId(), count);
    }

    @EventListener
    public void handleSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();
        String sessionId = accessor.getSessionId();

        if (destination != null) {
            activeSessions.put(sessionId, destination);
            log.info("ws.subscribe sessionId={} destination={}", sessionId, destination);
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        int count = activeConnections.decrementAndGet();
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        activeSessions.remove(sessionId);
        log.info("ws.disconnect sessionId={} total_active={}", sessionId, count);
    }

    public int getActiveConnectionCount() {
        return activeConnections.get();
    }
}
