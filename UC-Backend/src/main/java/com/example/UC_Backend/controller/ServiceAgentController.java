package com.example.UC_Backend.controller;

import com.example.UC_Backend.Database.ServiceAgentRepository;
import com.example.UC_Backend.Users.ServiceAgent;
import com.example.UC_Backend.common.ApiResponse;
import com.example.UC_Backend.exception.ResourceNotFoundException;
import com.example.UC_Backend.security.JwtTokenProvider;
import com.example.UC_Backend.Database.OrderRepository;
import com.example.UC_Backend.Order;
import java.util.HashMap;
import java.util.Optional;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * GET  /api/v1/agents/{agentId}/profile  — agent profile
 * GET  /api/v1/agents/{agentId}/orders   — pending + completed orders for agent
 * GET  /api/v1/agents                    — all agents (admin only)
 */
@RestController
@RequestMapping("/api/v1/agents")
public class ServiceAgentController {

    private final ServiceAgentRepository agentRepo;
    private final OrderRepository orderRepo;
    private final JwtTokenProvider jwtTokenProvider;

    public ServiceAgentController(ServiceAgentRepository agentRepo, OrderRepository orderRepo, JwtTokenProvider jwtTokenProvider) {
        this.agentRepo = agentRepo;
        this.orderRepo = orderRepo;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /** GET /api/v1/agents/me — returns the currently authenticated agent's own profile */
    @GetMapping("/me")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<ApiResponse<ServiceAgent>> getMyProfile(HttpServletRequest request) {
        String token = resolveToken(request);
        int agentId = jwtTokenProvider.getUserIdFromToken(token);
        ServiceAgent agent = agentRepo.findByAgentId(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceAgent", agentId));
        return ResponseEntity.ok(ApiResponse.ok(agent));
    }

    @GetMapping("/{agentId}/profile")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<ServiceAgent>> getAgentProfile(
            @PathVariable int agentId) {
        ServiceAgent agent = agentRepo.findByAgentId(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceAgent", agentId));
        return ResponseEntity.ok(ApiResponse.ok(agent));
    }

    @GetMapping("/{agentId}/orders")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> getAgentOrders(
            @PathVariable int agentId) {
        ServiceAgent agent = agentRepo.findByAgentId(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceAgent", agentId));

        java.util.Map<String, List<Order>> pendingOrders = new HashMap<>();
        for (String orderIdStr : agent.getPendingOrderIds()) {
            Optional<Order> o = orderRepo.findByOrderId(Integer.parseInt(orderIdStr));
            if (o.isPresent()) {
                Order order = o.get();
                for (String itemId : order.getCart()) {
                    pendingOrders.computeIfAbsent(itemId, k -> new ArrayList<>()).add(order);
                }
            }
        }

        java.util.Map<String, List<Order>> completedOrders = new HashMap<>();
        for (String orderIdStr : agent.getCompletedOrderIds()) {
            Optional<Order> o = orderRepo.findByOrderId(Integer.parseInt(orderIdStr));
            if (o.isPresent()) {
                Order order = o.get();
                for (String itemId : order.getCart()) {
                    completedOrders.computeIfAbsent(itemId, k -> new ArrayList<>()).add(order);
                }
            }
        }

        java.util.Map<String, Object> response = new HashMap<>();
        response.put("pending_orders", pendingOrders);
        response.put("completed_orders", completedOrders);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ServiceAgent>>> getAllAgents() {
        List<ServiceAgent> agents = new ArrayList<>();
        agentRepo.findAll().forEach(agents::add);
        return ResponseEntity.ok(ApiResponse.ok(agents));
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
