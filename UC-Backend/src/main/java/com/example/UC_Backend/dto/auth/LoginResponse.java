package com.example.UC_Backend.dto.auth;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String role,
        int userId,
        String name,
        String email
) {}
