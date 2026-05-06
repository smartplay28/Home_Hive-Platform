package com.example.UC_Backend.Users;

import com.example.UC_Backend.Extra.ExtraFunctions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Service agent document with compound indexes + 2dsphere geospatial index.
 *
 * Key indexes:
 *   - (location, skill)   — finding agents by area + capability: replaces full-scan RangeChecker
 *   - agentId (unique)    — O(1) agent lookup by ID
 *   - geoCoordinates (2dsphere) — native MongoDB geospatial queries ($near, $geoWithin)
 *                                 enables `db.agents.find({ geoCoordinates: { $near: { ... } } })`
 *
 * Implements Serializable for Redis cache serialization.
 *
 * MIGRATION NOTE:
 *   Existing agents with string "location" continue to work (backward compatible).
 *   New agents should also provide geoCoordinates [longitude, latitude] for
 *   full geospatial query support.
 */
@Document(collection = "service_agents")
@CompoundIndexes({
    @CompoundIndex(name = "location_skill_idx",      def = "{'location': 1, 'skill': 1}"),
    @CompoundIndex(name = "location_available_idx",  def = "{'location': 1, 'active': 1}")
})
public class ServiceAgent extends User implements Serializable {

    private static final long serialVersionUID = 1L;
    private transient ExtraFunctions func = new ExtraFunctions();

    @Id
    private String id = UUID.randomUUID().toString();

    @Indexed(unique = true)
    private int agentId;

    /** Service range in km — used for geospatial filtering */
    private int range;

    /** Skill categories this agent can serve, e.g. ["ACRepair", "Salon"] */
    @Indexed
    private String[] skill;

    /** Human-readable location area (e.g. "Koramangala") */
    @Indexed
    private String location;

    /**
     * GeoJSON coordinates [longitude, latitude] for 2dsphere queries.
     * If not set, system falls back to string-based RangeChecker.
     *
     * Example: [77.6245, 12.9352]  (Koramangala, Bangalore)
     *
     * Note: MongoDB 2dsphere requires [longitude, latitude] order (GeoJSON spec),
     * which is the OPPOSITE of the common human convention of lat/lon.
     */
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    @Field("geoCoordinates")
    private double[] geoCoordinates;

    /** Average service rating (1.0–5.0), updated after each order completion */
    private double avgRating = 0.0;

    /** Total completed orders count — used for rating weighted average */
    private int completedOrderCount = 0;

    /**
     * References to pending order IDs (NOT embedded Order objects).
     *
     * DESIGN: We store only the orderId String reference instead of full Order
     * documents to avoid:
     *   1. Data duplication — Order lives only in the 'orders' collection
     *   2. Stale data — embedded copies won't reflect Order status updates
     *   3. Document bloat — MongoDB has a 16MB document size limit
     *
     * To fetch full order: orderRepository.findByOrderId(orderId)
     */
    private List<String> pendingOrderIds = new ArrayList<>();

    /** References to completed order IDs — same rationale as pendingOrderIds. */
    private List<String> completedOrderIds = new ArrayList<>();

    public ServiceAgent(String name, String email, String password,
                        String[] skill, int range, String location) {
        super(name, email, password);
        this.agentId = new ExtraFunctions().generateID();
        this.skill = skill;
        this.range = range;
        this.location = location;
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────

    public int getAgentId()                      { return agentId; }
    public String getPassword()                  { return super.getPassword(); }
    public String getName()                      { return super.name; }
    public String getEmail()                     { return super.email; }
    public String[] getSkill()                   { return skill; }
    public int getRange()                        { return range; }
    public String getLocation()                  { return location; }
    public double[] getGeoCoordinates()          { return geoCoordinates; }
    public void setGeoCoordinates(double[] geo)  { this.geoCoordinates = geo; }
    public double getAvgRating()                 { return avgRating; }
    public int getCompletedOrderCount()          { return completedOrderCount; }

    public List<String> getPendingOrderIds()     { return pendingOrderIds; }
    public List<String> getCompletedOrderIds()   { return completedOrderIds; }

    /** Add a new pending order reference */
    public void addPendingOrderId(int orderId) {
        if (!pendingOrderIds.contains(orderId)) {
            pendingOrderIds.add(orderId);
        }
    }

    /** Move order from pending → completed */
    public void completeOrder(int orderId) {
        pendingOrderIds.remove(orderId);
        if (!completedOrderIds.contains(orderId)) {
            completedOrderIds.add(orderId);
        }
    }

    /** Remove a rejected/cancelled order from pending */
    public void removePendingOrder(int orderId) {
        pendingOrderIds.remove(orderId);
    }

    /**
     * Updates the running average rating using the Welford online algorithm.
     * O(1) — no need to store all historical ratings.
     *
     * Interview talking point: "I chose Welford's method to avoid storing full
     * rating history. Each update is O(1) and maintains a numerically stable average."
     */
    public void updateRating(double newRating) {
        this.completedOrderCount++;
        this.avgRating = avgRating + (newRating - avgRating) / completedOrderCount;
    }

    /** Check if agent has required skill */
    public boolean hasSkill(String serviceId) {
        return Arrays.stream(skill)
                .anyMatch(s -> s.equalsIgnoreCase(serviceId) || serviceId.startsWith(s));
    }

    /** True if agent has geo coordinates set for 2dsphere queries */
    public boolean hasGeoCoordinates() {
        return geoCoordinates != null && geoCoordinates.length == 2;
    }
}
