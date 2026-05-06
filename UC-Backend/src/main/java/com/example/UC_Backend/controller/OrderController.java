package com.example.UC_Backend.controller;

import com.example.UC_Backend.Database.CustomerRepository;
import com.example.UC_Backend.Database.ServiceAgentRepository;
import com.example.UC_Backend.Order;
import com.example.UC_Backend.Users.Customer;
import com.example.UC_Backend.Users.ServiceAgent;
import com.example.UC_Backend.common.ApiResponse;
import com.example.UC_Backend.exception.ResourceNotFoundException;
import com.example.UC_Backend.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Versioned order controller — JWT protected.
 * POST /api/v1/orders/checkout      (CUSTOMER)
 * GET  /api/v1/orders/history       (CUSTOMER)
 * POST /api/v1/orders/accept        (AGENT)
 * POST /api/v1/orders/reject        (AGENT)
 * GET  /api/v1/orders/cart/{id}     (CUSTOMER)
 * POST /api/v1/orders/cart/add      (CUSTOMER)
 * POST /api/v1/orders/cart/remove   (CUSTOMER)
 */
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;
    private final CustomerRepository customerRepo;
    private final ServiceAgentRepository agentRepo;

    public OrderController(OrderService orderService,
                           CustomerRepository customerRepo,
                           ServiceAgentRepository agentRepo) {
        this.orderService = orderService;
        this.customerRepo = customerRepo;
        this.agentRepo = agentRepo;
    }

    /** Place a new order — returns 202 Accepted immediately, agent matching is async */
    @PostMapping("/checkout")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> checkout(
            @RequestBody Map<String, Object> body) {
        int customerId = Integer.parseInt(body.get("customerId").toString());
        int totalPrice = Integer.parseInt(body.get("totalprice").toString());
        String location = (String) body.get("location");

        Order order = orderService.createOrder(customerId, totalPrice, location);
        orderService.assignAgents(order); // async fire-and-forget

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.ok("Order placed. Finding agents near you...",
                        Map.of("orderId", order.getOrderId())));
    }

    /** Get order history for a customer */
    @PostMapping("/history")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<Order>>> getOrderHistory(
            @RequestBody Map<String, Integer> body) {
        int customerId = Integer.parseInt(body.get("customerId").toString());
        List<Order> orders = orderService.getOrderHistory(customerId);
        return ResponseEntity.ok(ApiResponse.ok(orders));
    }

    /** Agent accepts an order */
    @PostMapping("/accept")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> acceptOrder(@RequestBody Map<String, Object> body) {
        int orderId = Integer.parseInt(body.get("orderId").toString());
        int agentId = Integer.parseInt(body.get("agentId").toString());
        String itemId = (String) body.get("itemId");
        orderService.acceptOrder(orderId, agentId, itemId);
        return ResponseEntity.ok(ApiResponse.ok("Order accepted successfully.", null));
    }

    @PostMapping("/reject")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> rejectOrder(@RequestBody Map<String, Object> body) {
        int orderId = Integer.parseInt(body.get("orderId").toString());
        int agentId = Integer.parseInt(body.get("agentId").toString());
        String itemId = (String) body.get("itemId");
        orderService.rejectOrder(orderId, agentId, itemId);
        return ResponseEntity.ok(ApiResponse.ok("Order rejected.", null));
    }

    /** Agent starts an order */
    @PostMapping("/start")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> startOrder(@RequestBody Map<String, Object> body) {
        int orderId = Integer.parseInt(body.get("orderId").toString());
        int agentId = Integer.parseInt(body.get("agentId").toString());
        orderService.startOrder(orderId, agentId);
        return ResponseEntity.ok(ApiResponse.ok("Order started successfully.", null));
    }

    /** Agent completes an order */
    @PostMapping("/complete")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> completeOrder(@RequestBody Map<String, Object> body) {
        int orderId = Integer.parseInt(body.get("orderId").toString());
        int agentId = Integer.parseInt(body.get("agentId").toString());
        orderService.completeOrder(orderId, agentId);
        return ResponseEntity.ok(ApiResponse.ok("Order completed successfully.", null));
    }

    /** Customer rates the agent after service completion */
    @PostMapping("/rate")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> rateOrder(@RequestBody Map<String, Object> body) {
        int orderId = Integer.parseInt(body.get("orderId").toString());
        double rating = Double.parseDouble(body.get("rating").toString());

        Order order = orderService.getOrderById(orderId);
        if (!"COMPLETED".equals(order.getOrderStatus())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Can only rate completed orders."));
        }
        if (order.getAgentId() == 0) {
            return ResponseEntity.badRequest().body(ApiResponse.error("No agent assigned to this order."));
        }

        ServiceAgent agent = agentRepo.findByAgentId(order.getAgentId())
                .orElseThrow(() -> new ResourceNotFoundException("Agent", order.getAgentId()));
        agent.updateRating(rating);
        agentRepo.save(agent);

        order.setOrderStatus("RATED");
        orderService.saveOrder(order);

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("agentName", agent.getName());
        result.put("newAvgRating", Math.round(agent.getAvgRating() * 10.0) / 10.0);
        return ResponseEntity.ok(ApiResponse.ok("Thank you for your rating!", result));
    }

    // ─── Cart ─────────────────────────────────────────────────────────────────

    /** Get all cart item IDs for a customer */
    @GetMapping("/cart/{customerId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, List<String>>>> getCartItems(
            @PathVariable int customerId) {
        Customer customer = customerRepo.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));
        return ResponseEntity.ok(ApiResponse.ok(Map.of("cartItems", customer.getShoppingCart())));
    }

    /** Add a service to cart */
    @PostMapping("/cart/add")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Void>> addToCart(@RequestBody Map<String, Object> body) {
        int customerId = Integer.parseInt(body.get("customerId").toString());
        String serviceId = (String) body.get("serviceId");

        Customer customer = customerRepo.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));

        if (customer.getShoppingCart().contains(serviceId)) {
            return ResponseEntity.ok(ApiResponse.ok("Service already in cart.", null));
        }
        customer.getShoppingCart().add(serviceId);
        customerRepo.save(customer);
        return ResponseEntity.ok(ApiResponse.ok("Service added to cart.", null));
    }

    /** Remove a service from cart */
    @PostMapping("/cart/remove")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Void>> removeFromCart(@RequestBody Map<String, Object> body) {
        int customerId = Integer.parseInt(body.get("customerId").toString());
        String serviceId = (String) body.get("serviceId");

        Customer customer = customerRepo.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));

        customer.getShoppingCart().remove(serviceId);
        customerRepo.save(customer);
        return ResponseEntity.ok(ApiResponse.ok("Service removed from cart.", null));
    }

    /** Get all orders for admin dashboard */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Order>>> getAllOrders() {
        return ResponseEntity.ok(ApiResponse.ok("All orders retrieved.", orderService.getAllOrders()));
    }
}
