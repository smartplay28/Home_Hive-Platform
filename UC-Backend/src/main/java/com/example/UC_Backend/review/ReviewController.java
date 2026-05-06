package com.example.UC_Backend.review;

import com.example.UC_Backend.common.ApiResponse;
import com.example.UC_Backend.dto.review.ReviewRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * POST /api/v1/reviews                          — submit review (CUSTOMER)
 * GET  /api/v1/reviews/agents/{agentId}         — get agent's reviews (any auth)
 * GET  /api/v1/reviews/me                        — customer's own reviews
 * GET  /api/v1/reviews/check/{orderId}           — has customer reviewed this order?
 * GET  /api/v1/reviews/agents/{agentId}/sentiment — admin sentiment analytics
 */
@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Review>> submitReview(
            @Valid @RequestBody ReviewRequest request,
            Authentication auth) {
        // Get customerId from JWT claims via Security context
        int customerId = getCustomerIdFromAuth(auth);
        Review review = reviewService.submitReview(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Review submitted successfully.", review));
    }

    @GetMapping("/agents/{agentId}")
    public ResponseEntity<ApiResponse<List<Review>>> getAgentReviews(
            @PathVariable int agentId) {
        return ResponseEntity.ok(ApiResponse.ok(reviewService.getAgentReviews(agentId)));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<Review>>> getMyReviews(Authentication auth) {
        int customerId = getCustomerIdFromAuth(auth);
        return ResponseEntity.ok(ApiResponse.ok(reviewService.getCustomerReviews(customerId)));
    }

    @GetMapping("/check/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> hasReviewed(
            @PathVariable int orderId,
            Authentication auth) {
        int customerId = getCustomerIdFromAuth(auth);
        boolean reviewed = reviewService.hasReviewed(customerId, orderId);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("hasReviewed", reviewed)));
    }

    @GetMapping("/agents/{agentId}/sentiment")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSentiment(
            @PathVariable int agentId) {
        return ResponseEntity.ok(ApiResponse.ok(reviewService.getAgentSentimentSummary(agentId)));
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    /**
     * Extract customerId from JWT via Spring Security context.
     * The JWT was parsed in JwtAuthFilter — no DB call needed.
     */
    private int getCustomerIdFromAuth(Authentication auth) {
        // Principal is set to the email; customerId is in the JWT claims
        // For now use localStorage customerId (sent in request body by frontend)
        // Phase 5 will move this to pull directly from JWT claims
        return 0; // placeholder — ReviewRequest.customerId will be used in service
    }
}
