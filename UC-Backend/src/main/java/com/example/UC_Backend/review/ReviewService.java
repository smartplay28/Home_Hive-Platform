package com.example.UC_Backend.review;

import com.example.UC_Backend.Database.OrderRepository;
import com.example.UC_Backend.Database.ServiceAgentRepository;
import com.example.UC_Backend.Order;
import com.example.UC_Backend.Users.ServiceAgent;
import com.example.UC_Backend.dto.review.ReviewRequest;
import com.example.UC_Backend.exception.ConflictException;
import com.example.UC_Backend.exception.ResourceNotFoundException;
import com.example.UC_Backend.exception.UnauthorizedException;
import com.example.UC_Backend.service.ServiceAgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Handles review lifecycle:
 *   1. Validate: order must be AGENT_ASSIGNED (or later), customer must have placed the order
 *   2. Prevent duplicates: one review per (customerId, orderId)
 *   3. Save review document
 *   4. Update agent's running average rating (Welford's O(1) method)
 *   5. Evict agent cache so new rating is reflected immediately
 */
@Service
public class ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);

    private static final List<String> VALID_ASPECTS = List.of(
            "punctuality", "quality", "cleanliness", "pricing", "communication"
    );

    private final ReviewRepository reviewRepo;
    private final OrderRepository orderRepo;
    private final ServiceAgentRepository agentRepo;
    private final ServiceAgentService agentService;

    public ReviewService(ReviewRepository reviewRepo,
                         OrderRepository orderRepo,
                         ServiceAgentRepository agentRepo,
                         ServiceAgentService agentService) {
        this.reviewRepo  = reviewRepo;
        this.orderRepo   = orderRepo;
        this.agentRepo   = agentRepo;
        this.agentService = agentService;
    }

    /**
     * Submit a review. Validates ownership + order status before persisting.
     * Triggers Welford rating update on the agent, then evicts agent cache.
     */
    @CacheEvict(value = "agents", key = "#request.agentId()")
    public Review submitReview(int customerId, ReviewRequest request) {
        // 1. Verify the order exists and belongs to this customer
        Order order = orderRepo.findByOrderId(request.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", request.orderId()));

        if (order.getCustomerId() != customerId) {
            throw new UnauthorizedException("You can only review your own orders.");
        }

        // 2. Order must have an assigned agent (can't review if agent never showed up)
        if (!"AGENT_ASSIGNED".equals(order.getOrderStatus())
                && !"COMPLETED".equals(order.getOrderStatus())) {
            throw new UnauthorizedException(
                    "You can only leave a review after an agent has been assigned.");
        }

        // 3. Prevent duplicate reviews
        if (reviewRepo.findByOrderIdAndCustomerId(request.orderId(), customerId).isPresent()) {
            throw new ConflictException("You have already submitted a review for this order.");
        }

        // 4. Build and save review
        Review review = new Review(
                request.agentId(), customerId, request.orderId(),
                request.rating(), request.comment(), request.serviceId());

        // Filter aspects to only valid values (prevent garbage data)
        if (request.positiveAspects() != null) {
            review.setPositiveAspects(
                    request.positiveAspects().stream()
                            .filter(VALID_ASPECTS::contains)
                            .toList());
        }
        if (request.negativeAspects() != null) {
            review.setNegativeAspects(
                    request.negativeAspects().stream()
                            .filter(VALID_ASPECTS::contains)
                            .toList());
        }

        reviewRepo.save(review);
        log.info("review.submit orderId={} agentId={} rating={}", request.orderId(),
                request.agentId(), request.rating());

        // 5. Update agent's running average (Welford O(1) — no recomputation of all reviews)
        ServiceAgent agent = agentRepo.findByAgentId(request.agentId())
                .orElseThrow(() -> new ResourceNotFoundException("Agent", request.agentId()));

        agent.updateRating(request.rating());
        agentService.saveAgent(agent); // saves + evicts agent cache

        log.info("review.rating.updated agentId={} newAvg={:.2f} totalReviews={}",
                request.agentId(), agent.getAvgRating(), agent.getCompletedOrderCount());

        return review;
    }

    /** Get latest 10 reviews for an agent's profile page */
    public List<Review> getAgentReviews(int agentId) {
        return reviewRepo.findTop10ByAgentIdOrderByCreatedAtDesc(agentId);
    }

    /** Get all reviews by a customer */
    public List<Review> getCustomerReviews(int customerId) {
        return reviewRepo.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    /** Check if customer has already reviewed a specific order */
    public boolean hasReviewed(int customerId, int orderId) {
        return reviewRepo.findByOrderIdAndCustomerId(orderId, customerId).isPresent();
    }

    /**
     * Admin analytics: aspect sentiment breakdown.
     * Returns count of each aspect mentioned as positive or negative.
     * Powers the admin dashboard sentiment widget.
     */
    public Map<String, Object> getAgentSentimentSummary(int agentId) {
        List<Review> reviews = reviewRepo.findByAgentIdOrderByCreatedAtDesc(agentId);

        Map<String, Long> positiveCount = new java.util.HashMap<>();
        Map<String, Long> negativeCount = new java.util.HashMap<>();

        for (Review r : reviews) {
            if (r.getPositiveAspects() != null) {
                r.getPositiveAspects().forEach(a ->
                        positiveCount.merge(a, 1L, Long::sum));
            }
            if (r.getNegativeAspects() != null) {
                r.getNegativeAspects().forEach(a ->
                        negativeCount.merge(a, 1L, Long::sum));
            }
        }

        return Map.of(
                "totalReviews", reviews.size(),
                "positiveAspects", positiveCount,
                "negativeAspects", negativeCount
        );
    }
}
