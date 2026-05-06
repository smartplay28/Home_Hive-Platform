package com.example.UC_Backend.schedule;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScheduleRepository extends MongoRepository<AgentSchedule, String> {
    Optional<AgentSchedule> findByAgentId(int agentId);
}
