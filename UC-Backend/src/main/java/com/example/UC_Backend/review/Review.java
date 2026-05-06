package com.example.UC_Backend.review;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Review document — one review per (orderId, customerId) pair.
 *
 * Compound indexes:
 *   - (agentId, createdAt) — agent's review timeline (newest first)
 *   - (customerId, orderId) — prevent duplicate reviews + customer history
 *
 * Rating is stored both on this document AND as a running average on ServiceAgent
 * (Welford's method) — no aggregation query needed to display agent rating.
 */
@Document(collection = "reviews")
@CompoundIndexes({
    @CompoundIndex(name = "agent_created_idx",   def = "{'agentId': 1, 'createdAt': -1}"),
    @CompoundIndex(name = "customer_order_idx",  def = "{'customerId': 1, 'orderId': 1}", unique = true)
})
public class Review implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id = UUID.randomUUID().toString();

    @Indexed
    private int agentId;

    @Indexed
    private int customerId;

    @Indexed(unique = true)
    private int orderId;

    /** Rating from 1 to 5 */
    private int rating;

    /** Optional text feedback */
    private String comment;

    /**
     * Aspect tags the customer selected — from a fixed set:
     * ["punctuality", "quality", "cleanliness", "pricing", "communication"]
     * Used for NLP sentiment analysis in Phase 7.
     */
    private List<String> positiveAspects;
    private List<String> negativeAspects;

    /** Service category this review is for */
    private String serviceId;

    @CreatedDate
    private Instant createdAt;

    // Default constructor for MongoDB
    public Review() {}

    public Review(int agentId, int customerId, int orderId, int rating,
                  String comment, String serviceId) {
        this.agentId     = agentId;
        this.customerId  = customerId;
        this.orderId     = orderId;
        this.rating      = rating;
        this.comment     = comment;
        this.serviceId   = serviceId;
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────

    public String getId()                              { return id; }
    public int getAgentId()                            { return agentId; }
    public int getCustomerId()                         { return customerId; }
    public int getOrderId()                            { return orderId; }
    public int getRating()                             { return rating; }
    public String getComment()                         { return comment; }
    public List<String> getPositiveAspects()           { return positiveAspects; }
    public void setPositiveAspects(List<String> pos)   { this.positiveAspects = pos; }
    public List<String> getNegativeAspects()           { return negativeAspects; }
    public void setNegativeAspects(List<String> neg)   { this.negativeAspects = neg; }
    public String getServiceId()                       { return serviceId; }
    public Instant getCreatedAt()                      { return createdAt; }
}
