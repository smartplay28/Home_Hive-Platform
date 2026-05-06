package com.example.UC_Backend.controller;

import com.example.UC_Backend.Database.CustomerRepository;
import com.example.UC_Backend.Users.Customer;
import com.example.UC_Backend.common.ApiResponse;
import com.example.UC_Backend.exception.ResourceNotFoundException;
import com.example.UC_Backend.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * GET /api/v1/customers/me       — get own profile (via JWT userId)
 * POST /api/v1/customers/profile — get profile by customerId (legacy support)
 */
@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerRepository customerRepo;
    private final JwtTokenProvider jwtTokenProvider;

    public CustomerController(CustomerRepository customerRepo, JwtTokenProvider jwtTokenProvider) {
        this.customerRepo = customerRepo;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /** GET /api/v1/customers/me — returns the currently authenticated customer's profile */
    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyProfile(HttpServletRequest request) {
        String token = resolveToken(request);
        int customerId = jwtTokenProvider.getUserIdFromToken(token);
        Customer customer = customerRepo.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));
        Map<String, Object> profile = new java.util.HashMap<>();
        profile.put("name", customer.getName());
        profile.put("email", customer.getEmail());
        profile.put("phone", customer.getPhone());
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }

    @GetMapping("/{customerId}/profile")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProfile(
            @PathVariable int customerId) {
        Customer customer = customerRepo.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));

        Map<String, Object> profile = new java.util.HashMap<>();
        profile.put("name", customer.getName());
        profile.put("email", customer.getEmail());
        profile.put("phone", customer.getPhone());
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
