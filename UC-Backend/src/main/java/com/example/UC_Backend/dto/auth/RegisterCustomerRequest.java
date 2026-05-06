package com.example.UC_Backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterCustomerRequest(
        @NotBlank @Size(min = 2) String name,
        @NotBlank @Email String email,
        long phone,
        @NotBlank @Size(min = 6) String password
) {}
