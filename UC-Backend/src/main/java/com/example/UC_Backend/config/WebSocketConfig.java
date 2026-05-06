package com.example.UC_Backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket + STOMP configuration.
 *
 * Clients subscribe to topics using the STOMP protocol:
 *
 *   AGENT receives:
 *     /topic/agents/{agentId}/new-order      — new order notification
 *     /topic/agents/{agentId}/order-cancelled — order was cancelled
 *
 *   CUSTOMER receives:
 *     /topic/customers/{customerId}/order-update — status change (AGENT_ASSIGNED, etc.)
 *
 *   ADMIN receives:
 *     /topic/admin/orders — global order feed
 *
 * Clients connect via SockJS at: ws://localhost:8080/ws
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable in-memory broker for /topic (broadcast) and /queue (point-to-point)
        registry.enableSimpleBroker("/topic", "/queue");
        // Prefix for messages sent FROM client TO server
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")   // fine for dev; lock to domain in prod
                .withSockJS();                   // SockJS fallback for older browsers
    }
}
