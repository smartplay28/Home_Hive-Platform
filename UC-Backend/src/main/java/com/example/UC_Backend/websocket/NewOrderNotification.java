package com.example.UC_Backend.websocket;

import java.time.Instant;

/**
 * Payload sent to a SERVICE AGENT when a new order arrives that matches their skill + location.
 * Subscribed via: /topic/agents/{agentId}/new-order
 */
public record NewOrderNotification(
        int orderId,
        int customerId,
        String serviceId,
        String serviceName,
        String location,
        int totalPrice,
        Instant timestamp
) {
    public static NewOrderNotification of(int orderId, int customerId,
                                          String serviceId, String serviceName,
                                          String location, int totalPrice) {
        return new NewOrderNotification(orderId, customerId, serviceId, serviceName,
                location, totalPrice, Instant.now());
    }
}
