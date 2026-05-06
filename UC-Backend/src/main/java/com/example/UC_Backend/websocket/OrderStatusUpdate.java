package com.example.UC_Backend.websocket;

import java.time.Instant;

/**
 * Payload sent to a CUSTOMER when their order status changes.
 * Subscribed via: /topic/customers/{customerId}/order-update
 *
 * Statuses:
 *   PENDING_NOT_ASSIGNED  → just placed, finding agents
 *   AGENT_ASSIGNED        → an agent accepted
 *   IN_PROGRESS           → agent has started work
 *   COMPLETED             → work done
 *   CANCELLED             → no agents available or customer cancelled
 */
public record OrderStatusUpdate(
        int orderId,
        String newStatus,
        String message,
        Integer assignedAgentId,    // null until an agent accepts
        String assignedAgentName,   // null until an agent accepts
        Instant timestamp
) {
    /** Order just placed, finding agents */
    public static OrderStatusUpdate pending(int orderId) {
        return new OrderStatusUpdate(orderId, "PENDING_NOT_ASSIGNED",
                "We're finding the best agent near you...", null, null, Instant.now());
    }

    /** An agent accepted the order */
    public static OrderStatusUpdate agentAssigned(int orderId, int agentId, String agentName) {
        return new OrderStatusUpdate(orderId, "AGENT_ASSIGNED",
                agentName + " has accepted your request and will be there soon!",
                agentId, agentName, Instant.now());
    }

    /** An agent rejected — still looking */
    public static OrderStatusUpdate agentRejected(int orderId) {
        return new OrderStatusUpdate(orderId, "PENDING_NOT_ASSIGNED",
                "An agent declined. Still looking for another agent...",
                null, null, Instant.now());
    }
}
