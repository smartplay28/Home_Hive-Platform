package com.example.UC_Backend.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Central WebSocket notification service.
 * Injected into OrderService so it can push messages after business logic completes.
 *
 * Uses SimpMessagingTemplate (Spring's STOMP messenger) — no direct WebSocket handling needed.
 */
@Service
public class OrderNotificationService {

    private static final Logger log = LoggerFactory.getLogger(OrderNotificationService.class);

    private final SimpMessagingTemplate messagingTemplate;

    public OrderNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Notifies a specific agent about a new incoming order request.
     * Agent subscribes to: /topic/agents/{agentId}/new-order
     */
    public void notifyAgentNewOrder(int agentId, NewOrderNotification notification) {
        String destination = "/topic/agents/" + agentId + "/new-order";
        messagingTemplate.convertAndSend(destination, notification);
        log.info("ws.agent.notify agentId={} orderId={} service={}",
                agentId, notification.orderId(), notification.serviceId());
    }

    /**
     * Sends order status update to the customer who placed the order.
     * Customer subscribes to: /topic/customers/{customerId}/order-update
     */
    public void notifyCustomerOrderUpdate(int customerId, OrderStatusUpdate update) {
        String destination = "/topic/customers/" + customerId + "/order-update";
        messagingTemplate.convertAndSend(destination, update);
        log.info("ws.customer.notify customerId={} orderId={} status={}",
                customerId, update.orderId(), update.newStatus());
    }

    /**
     * Broadcasts a new order to the admin global feed.
     * Admin subscribes to: /topic/admin/orders
     */
    public void broadcastToAdmin(NewOrderNotification notification) {
        messagingTemplate.convertAndSend("/topic/admin/orders", notification);
        log.debug("ws.admin.broadcast orderId={}", notification.orderId());
    }
}
