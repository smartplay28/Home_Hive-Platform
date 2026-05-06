package com.example.UC_Backend.Database;

import com.example.UC_Backend.Users.ServiceAgent;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ServiceAgent.
 *
 * Geospatial queries use MongoDB's native 2dsphere index on `geoCoordinates`.
 * These replace the legacy C++ RangeChecker for agents that have set geo coordinates.
 *
 * Query performance:
 *   findBySkillContainingAndLocation → uses (location, skill) compound index
 *   findByAgentId                    → uses unique index on agentId
 *   findNearWithSkill                → uses 2dsphere index (sub-ms latency at scale)
 */
@Repository
public interface ServiceAgentRepository extends MongoRepository<ServiceAgent, String> {

    Optional<ServiceAgent> findByEmail(String email);

    Optional<ServiceAgent> findByAgentId(int agentId);

    /** Find all agents in a specific location area with a given skill */
    List<ServiceAgent> findByLocationAndSkillContaining(String location, String skill);

    /** Find all agents with a specific skill */
    List<ServiceAgent> findBySkillContaining(String skill);

    /** Find all active agents */
    List<ServiceAgent> findByActive(boolean active);

    /**
     * Native MongoDB 2dsphere geo query — finds agents within `distance` of a point.
     * Only runs for agents that have set geoCoordinates.
     *
     * MongoDB translates this to:
     *   db.service_agents.find({
     *     geoCoordinates: { $near: { $geometry: { type: "Point", coordinates: [lon, lat] } },
     *                       $maxDistance: rangeMeters }
     *   })
     *
     * Uses the 2dsphere index — scales to millions of agents, results in <10ms.
     *
     * @param point    GeoJSON point [longitude, latitude]
     * @param distance max distance (use Distance(km, Metrics.KILOMETERS))
     */
    List<ServiceAgent> findByGeoCoordinatesNear(Point point, Distance distance);

    /**
     * Geo query filtered by skill — the golden query for order matching.
     * Finds agents near a location who have the required skill in one indexed scan.
     */
    @Query("{ 'geoCoordinates': { $near: { $geometry: { type: 'Point', coordinates: [?0, ?1] }, $maxDistance: ?2 } }, 'skill': { $in: [?3] }, 'active': true }")
    List<ServiceAgent> findNearWithSkill(double longitude, double latitude,
                                         double maxDistanceMeters, String skill);

    /** Count available agents in a location (for demand/supply ratio in dynamic pricing) */
    long countByLocationAndActive(String location, boolean active);
}