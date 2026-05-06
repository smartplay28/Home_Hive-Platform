package com.example.UC_Backend.service;

import com.example.UC_Backend.Database.CustomerRepository;
import com.example.UC_Backend.Database.OrderRepository;
import com.example.UC_Backend.Database.ServiceAgentRepository;
import com.example.UC_Backend.Order;
import com.example.UC_Backend.Users.Customer;
import com.example.UC_Backend.Users.ServiceAgent;
import com.example.UC_Backend.exception.ResourceNotFoundException;
import com.example.UC_Backend.websocket.NewOrderNotification;
import com.example.UC_Backend.websocket.OrderNotificationService;
import com.example.UC_Backend.websocket.OrderStatusUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Handles all order lifecycle: creation, async agent matching, accept, reject.
 *
 * Phase 3 additions:
 *   - Order history is Redis-cached (60s TTL) — reduces MongoDB reads on history page
 *   - Agent matching uses ServiceAgentService for cached+geo-aware lookups
 *   - Cache is evicted on accept/reject to prevent stale order history
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepo;
    private final CustomerRepository customerRepo;
    private final ServiceAgentRepository agentRepo;
    private final OrderNotificationService notificationService;
    private final ServiceAgentService agentService;

    public OrderService(OrderRepository orderRepo,
                        CustomerRepository customerRepo,
                        ServiceAgentRepository agentRepo,
                        OrderNotificationService notificationService,
                        ServiceAgentService agentService) {
        this.orderRepo = orderRepo;
        this.customerRepo = customerRepo;
        this.agentRepo = agentRepo;
        this.notificationService = notificationService;
        this.agentService = agentService;
    }

    // ─── Create ───────────────────────────────────────────────────────────────

    /**
     * Creates the order synchronously — returns 202 Accepted immediately.
     * Evicts the cached order history so next fetch reflects this new order.
     */
    @CacheEvict(value = "orderHistory", key = "#customerId")
    public Order createOrder(int customerId, int totalPrice, String location) {
        Customer customer = customerRepo.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));

        Order order = new Order(customer.getCustomerId(), "PENDING_NOT_ASSIGNED", totalPrice, location);
        order.setCart(customer.getShoppingCart());
        orderRepo.save(order);

        notificationService.notifyCustomerOrderUpdate(customerId,
                OrderStatusUpdate.pending(order.getOrderId()));

        log.info("order.create orderId={} customerId={} location={} items={}",
                order.getOrderId(), customerId, location, order.getCart().size());
        return order;
    }

    // ─── Async Agent Matching ─────────────────────────────────────────────────

    /**
     * Runs in background thread pool. Matches agents and notifies them via WebSocket.
     * Uses ServiceAgentService for hybrid geo+legacy matching with Redis caching.
     */
    @Async("orderProcessingExecutor")
    public CompletableFuture<Void> assignAgents(Order order) {
        try {
            ArrayList<ServiceAgent> allAgents = new ArrayList<>();
            agentRepo.findAll().forEach(allAgents::add);

            int totalNotified = 0;

            for (String itemId : order.getCart()) {
                // Hybrid: geo-based + legacy string-based matching
                List<ServiceAgent> nearbyAgents = agentService.findNearbyAgentsHybrid(
                        order.getLocation(), itemId, allAgents);

                for (ServiceAgent agent : nearbyAgents) {
                    agent.getPendingOrderIds().add(String.valueOf(order.getOrderId()));
                    order.getRequestAgents().add(agent.getAgentId());

                    // Use agentService.saveAgent() — saves + evicts agent cache atomically
                    agentService.saveAgent(agent);

                    // Push WebSocket notification to agent
                    NewOrderNotification notification = NewOrderNotification.of(
                            order.getOrderId(), order.getCustomerId(),
                            itemId, itemId, order.getLocation(), order.getTotalPrice());
                    notificationService.notifyAgentNewOrder(agent.getAgentId(), notification);
                    notificationService.broadcastToAdmin(notification);

                    totalNotified++;
                    log.debug("order.agent.notified orderId={} agentId={} item={}",
                            order.getOrderId(), agent.getAgentId(), itemId);
                }
                orderRepo.save(order);
            }

            log.info("order.assign.complete orderId={} agentsNotified={}", order.getOrderId(), totalNotified);

            if (totalNotified == 0) {
                notificationService.notifyCustomerOrderUpdate(order.getCustomerId(),
                        new OrderStatusUpdate(order.getOrderId(), "NO_AGENTS_AVAILABLE",
                                "Sorry, no agents are currently available in your area. We'll keep trying.",
                                null, null, java.time.Instant.now()));
            }

        } catch (Exception e) {
            log.error("order.assign.failed orderId={} error={}", order.getOrderId(), e.getMessage(), e);
        }
        return CompletableFuture.completedFuture(null);
    }

    // ─── Accept / Reject ──────────────────────────────────────────────────────

    /**
     * Agent accepts — evicts order history cache and agent cache.
     */
    @CacheEvict(value = "orderHistory", key = "#result?.customerId ?: 0")
    public Order acceptOrder(int orderId, int agentId, String itemId) {
        Order order = orderRepo.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        ServiceAgent acceptingAgent = agentRepo.findByAgentId(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent", agentId));

        for (Integer agentIdIter : order.getRequestAgents()) {
            Optional<ServiceAgent> saOpt = agentRepo.findByAgentId(agentIdIter);
            if (saOpt.isEmpty()) continue;
            ServiceAgent agent = saOpt.get();

            if (agentIdIter != agentId) {
                // Remove from other agents who didn't accept
                agent.getPendingOrderIds().remove(String.valueOf(orderId));
                agentService.saveAgent(agent);
            }
        }

        order.getRequestAgents().remove(Integer.valueOf(agentId));
        order.setAgentId(agentId);
        order.setOrderStatus("AGENT_ASSIGNED");
        orderRepo.save(order);

        notificationService.notifyCustomerOrderUpdate(order.getCustomerId(),
                OrderStatusUpdate.agentAssigned(orderId, agentId, acceptingAgent.getName()));

        log.info("order.accept orderId={} agentId={}", orderId, agentId);
        return order;
    }

    @CacheEvict(value = "orderHistory", key = "#result?.customerId ?: 0")
    public Order startOrder(int orderId, int agentId) {
        Order order = orderRepo.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        order.setOrderStatus("IN_PROGRESS");
        orderRepo.save(order);

        notificationService.notifyCustomerOrderUpdate(order.getCustomerId(),
                new OrderStatusUpdate(orderId, "IN_PROGRESS", "Your agent has started the service.", null, null, java.time.Instant.now()));

        log.info("order.start orderId={} agentId={}", orderId, agentId);
        return order;
    }

    @CacheEvict(value = "orderHistory", key = "#result?.customerId ?: 0")
    public Order completeOrder(int orderId, int agentId) {
        Order order = orderRepo.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        ServiceAgent agent = agentRepo.findByAgentId(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent", agentId));

        agent.getPendingOrderIds().remove(String.valueOf(orderId));
        if (!agent.getCompletedOrderIds().contains(String.valueOf(orderId))) {
            agent.getCompletedOrderIds().add(String.valueOf(orderId));
        }
        agentService.saveAgent(agent);

        order.setOrderStatus("COMPLETED");
        orderRepo.save(order);

        notificationService.notifyCustomerOrderUpdate(order.getCustomerId(),
                new OrderStatusUpdate(orderId, "COMPLETED", "Your service is complete!", null, null, java.time.Instant.now()));

        log.info("order.complete orderId={} agentId={}", orderId, agentId);
        return order;
    }

    public void rejectOrder(int orderId, int agentId, String itemId) {
        ServiceAgent agent = agentRepo.findByAgentId(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent", agentId));
        Order order = orderRepo.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        agent.getPendingOrderIds().remove(String.valueOf(orderId));

        order.getRequestAgents().remove(Integer.valueOf(agentId));
        orderRepo.save(order);
        agentService.saveAgent(agent); // saves + evicts cache

        notificationService.notifyCustomerOrderUpdate(order.getCustomerId(),
                OrderStatusUpdate.agentRejected(orderId));

        log.info("order.reject orderId={} agentId={}", orderId, agentId);
    }

    // ─── Read (Cached) ────────────────────────────────────────────────────────

    /**
     * Get order history — cached in Redis for 60s.
     * Cache key: "orderHistory::12345"
     * Cache miss: hits MongoDB (customerId, orderStatus) compound index
     * Cache hit: Redis GET → <1ms — important for order history page load
     */
    @Cacheable(value = "orderHistory", key = "#customerId")
    public List<Order> getOrderHistory(int customerId) {
        log.debug("cache.miss orderHistory customerId={}", customerId);
        return orderRepo.findByCustomerId(customerId);
    }

    public Order getOrderById(int orderId) {
        return orderRepo.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
    }

    @CacheEvict(value = "orderHistory", allEntries = true)
    public Order saveOrder(Order order) {
        return orderRepo.save(order);
    }

    public List<Order> getAllOrders() {
        return orderRepo.findAll();
    }
}
