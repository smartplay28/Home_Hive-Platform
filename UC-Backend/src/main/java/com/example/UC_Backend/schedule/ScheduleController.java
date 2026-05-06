package com.example.UC_Backend.schedule;

import com.example.UC_Backend.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * GET  /api/v1/schedules/{agentId}              — get agent's schedule (AGENT, ADMIN)
 * PUT  /api/v1/schedules/{agentId}/slots        — set weekly availability (AGENT)
 * POST /api/v1/schedules/{agentId}/blocked      — add blocked dates (AGENT)
 * DELETE /api/v1/schedules/{agentId}/blocked    — remove a blocked date (AGENT)
 * PUT  /api/v1/schedules/{agentId}/max-orders   — set max orders/day (AGENT)
 * GET  /api/v1/schedules/{agentId}/available    — is agent available right now? (internal)
 */
@RestController
@RequestMapping("/api/v1/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping("/{agentId}")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<AgentSchedule>> getSchedule(@PathVariable int agentId) {
        return ResponseEntity.ok(ApiResponse.ok(scheduleService.getOrCreateSchedule(agentId)));
    }

    @PutMapping("/{agentId}/slots")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<AgentSchedule>> setWeeklySlots(
            @PathVariable int agentId,
            @RequestBody List<AvailabilitySlot> slots) {
        AgentSchedule schedule = scheduleService.setWeeklySlots(agentId, slots);
        return ResponseEntity.ok(ApiResponse.ok("Weekly availability updated.", schedule));
    }

    @PostMapping("/{agentId}/blocked")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<AgentSchedule>> addBlockedDates(
            @PathVariable int agentId,
            @RequestBody List<String> dateStrings) {
        List<LocalDate> dates = dateStrings.stream().map(LocalDate::parse).toList();
        AgentSchedule schedule = scheduleService.addBlockedDates(agentId, dates);
        return ResponseEntity.ok(ApiResponse.ok("Blocked dates added.", schedule));
    }

    @DeleteMapping("/{agentId}/blocked")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<AgentSchedule>> removeBlockedDate(
            @PathVariable int agentId,
            @RequestParam String date) {
        AgentSchedule schedule = scheduleService.removeBlockedDate(agentId, LocalDate.parse(date));
        return ResponseEntity.ok(ApiResponse.ok("Date unblocked.", schedule));
    }

    @PutMapping("/{agentId}/max-orders")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> setMaxOrdersPerDay(
            @PathVariable int agentId,
            @RequestBody Map<String, Integer> body) {
        scheduleService.setMaxOrdersPerDay(agentId, body.get("maxOrdersPerDay"));
        return ResponseEntity.ok(ApiResponse.ok("Max orders per day set.", null));
    }

    @GetMapping("/{agentId}/available")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkAvailability(
            @PathVariable int agentId) {
        boolean available = scheduleService.isAvailableNow(agentId);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("available", available)));
    }
}
