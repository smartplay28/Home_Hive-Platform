package com.example.UC_Backend.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Standardized API response envelope.
 * Every endpoint returns this — no more bare strings like "SUCCESSFULL ADDED".
 *
 * Example success:
 *   { "success": true, "message": "Order placed", "data": {...}, "timestamp": "..." }
 *
 * Example error:
 *   { "success": false, "message": "Customer not found", "data": null, "timestamp": "..." }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        Instant timestamp
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "Success", data, Instant.now());
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data, Instant.now());
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, Instant.now());
    }
}
