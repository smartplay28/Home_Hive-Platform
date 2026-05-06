package com.example.UC_Backend.controller;

import com.example.UC_Backend.common.ApiResponse;
import com.example.UC_Backend.dto.auth.*;
import com.example.UC_Backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Versioned auth controller — public endpoints (no JWT required).
 * POST /api/v1/auth/customer/register
 * POST /api/v1/auth/customer/login
 * POST /api/v1/auth/agent/register
 * POST /api/v1/auth/agent/login
 * POST /api/v1/auth/admin/login
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ─── Customer ────────────────────────────────────────────────────────────

    @PostMapping("/customer/register")
    public ResponseEntity<ApiResponse<LoginResponse>> registerCustomer(
            @Valid @RequestBody RegisterCustomerRequest request) {
        LoginResponse response = authService.registerCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Customer registered successfully.", response));
    }

    @PostMapping("/customer/login")
    public ResponseEntity<ApiResponse<LoginResponse>> loginCustomer(
            @Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.loginCustomer(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful.", response));
    }

    // ─── Service Agent ────────────────────────────────────────────────────────

    @PostMapping("/agent/register")
    public ResponseEntity<ApiResponse<LoginResponse>> registerAgent(
            @Valid @RequestBody RegisterAgentRequest request) {
        LoginResponse response = authService.registerAgent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Service agent registered successfully.", response));
    }

    @PostMapping("/agent/login")
    public ResponseEntity<ApiResponse<LoginResponse>> loginAgent(
            @Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.loginAgent(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful.", response));
    }

    // ─── Admin ────────────────────────────────────────────────────────────────

    @PostMapping("/admin/register")
    public ResponseEntity<ApiResponse<LoginResponse>> registerAdmin(
            @Valid @RequestBody RegisterAgentRequest request) { // Reusing DTO with name/email/password
        LoginResponse response = authService.registerAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Admin registered successfully.", response));
    }

    @PostMapping("/admin/login")
    public ResponseEntity<ApiResponse<LoginResponse>> loginAdmin(
            @Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.loginAdmin(request);
        return ResponseEntity.ok(ApiResponse.ok("Admin login successful.", response));
    }
}
