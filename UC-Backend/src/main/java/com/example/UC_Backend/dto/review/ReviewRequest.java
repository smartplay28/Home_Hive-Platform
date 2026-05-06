package com.example.UC_Backend.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ReviewRequest(
        @NotNull int orderId,
        @NotNull int agentId,
        @NotNull String serviceId,
        @Min(1) @Max(5) int rating,
        String comment,
        List<String> positiveAspects,
        List<String> negativeAspects
) {}
