package com.example.UC_Backend.review;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Review repository.
 * All queries use compound indexes — no full collection scans.
 */
@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {

    /** Agent's reviews sorted by newest first — uses (agentId, createdAt) compound index */
    List<Review> findByAgentIdOrderByCreatedAtDesc(int agentId);

    /** Check if customer already reviewed this order — enforced by unique index too */
    Optional<Review> findByOrderIdAndCustomerId(int orderId, int customerId);

    /** All reviews for an order */
    Optional<Review> findByOrderId(int orderId);

    /** Customer's review history */
    List<Review> findByCustomerIdOrderByCreatedAtDesc(int customerId);

    /** Latest N reviews for an agent (for profile page) */
    List<Review> findTop10ByAgentIdOrderByCreatedAtDesc(int agentId);

    /** Count of reviews per agent */
    long countByAgentId(int agentId);

    /**
     * Average rating for an agent using MongoDB aggregation.
     * NOTE: We store the running average on ServiceAgent (Welford's method)
     * so this is only needed for admin analytics / audit.
     */
    @Query(value = "{'agentId': ?0}", fields = "{'rating': 1}")
    List<Review> findRatingsByAgentId(int agentId);
}
