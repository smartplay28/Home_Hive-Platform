package com.example.UC_Backend.schedule;

import com.example.UC_Backend.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Manages agent availability schedules.
 *
 * Key design decisions:
 *   - scheduleRestrictionEnabled defaults to false → existing agents unaffected
 *   - isAvailableNow() is used by OrderService to filter agents before notifying them
 *     (avoids bothering offline agents, reduces notification noise)
 *   - Blocked dates are exact LocalDate matches (not ranges) for simplicity
 *
 * Future: Add recurring exceptions (e.g. "Every 2nd Tuesday off") using RRULE from iCal spec.
 */
@Service
public class ScheduleService {

    private static final Logger log = LoggerFactory.getLogger(ScheduleService.class);

    private final ScheduleRepository scheduleRepo;

    public ScheduleService(ScheduleRepository scheduleRepo) {
        this.scheduleRepo = scheduleRepo;
    }

    /**
     * Get or create schedule for an agent (upsert pattern).
     * New agents get a default "unrestricted" schedule.
     */
    public AgentSchedule getOrCreateSchedule(int agentId) {
        return scheduleRepo.findByAgentId(agentId)
                .orElseGet(() -> {
                    AgentSchedule newSchedule = new AgentSchedule(agentId);
                    return scheduleRepo.save(newSchedule);
                });
    }

    /**
     * Set the agent's weekly availability slots.
     * Replaces all existing slots (idempotent update).
     */
    public AgentSchedule setWeeklySlots(int agentId, List<AvailabilitySlot> slots) {
        AgentSchedule schedule = getOrCreateSchedule(agentId);
        schedule.setWeeklySlots(slots);
        schedule.setScheduleRestrictionEnabled(true); // enable schedule enforcement
        AgentSchedule saved = scheduleRepo.save(schedule);
        log.info("schedule.update agentId={} slots={}", agentId, slots.size());
        return saved;
    }

    /**
     * Add blocked dates (days off, vacation).
     * Appends to existing blocked dates (doesn't replace).
     */
    public AgentSchedule addBlockedDates(int agentId, List<LocalDate> dates) {
        AgentSchedule schedule = getOrCreateSchedule(agentId);
        schedule.getBlockedDates().addAll(dates);
        return scheduleRepo.save(schedule);
    }

    /**
     * Remove a blocked date (agent became available again).
     */
    public AgentSchedule removeBlockedDate(int agentId, LocalDate date) {
        AgentSchedule schedule = getOrCreateSchedule(agentId);
        schedule.getBlockedDates().remove(date);
        return scheduleRepo.save(schedule);
    }

    /**
     * Core availability check — called before notifying an agent about an order.
     *
     * Algorithm:
     *   1. If schedule restrictions disabled → always available (backward compat)
     *   2. If today is a blocked date → not available
     *   3. Check if any weekly slot covers today's day + current hour
     *
     * O(k) where k = number of weekly slots (typically ≤ 7), effectively O(1).
     */
    public boolean isAvailableNow(int agentId) {
        AgentSchedule schedule = scheduleRepo.findByAgentId(agentId).orElse(null);

        // No schedule = no restrictions = always available
        if (schedule == null || !schedule.isScheduleRestrictionEnabled()) {
            return true;
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // Check blocked dates
        if (schedule.getBlockedDates().contains(today)) {
            log.debug("schedule.blocked agentId={} date={}", agentId, today);
            return false;
        }

        // Check weekly slots
        String todayName = DayOfWeek.from(today).name(); // "MONDAY", "TUESDAY", etc.
        int currentHour = now.getHour();

        boolean hasMatchingSlot = schedule.getWeeklySlots().stream()
                .anyMatch(slot ->
                        slot.getDayOfWeek().equalsIgnoreCase(todayName)
                        && currentHour >= slot.getStartHour()
                        && currentHour < slot.getEndHour()
                );

        if (!hasMatchingSlot) {
            log.debug("schedule.outside_hours agentId={} day={} hour={}", agentId, todayName, currentHour);
        }

        return hasMatchingSlot;
    }

    public void setMaxOrdersPerDay(int agentId, int max) {
        AgentSchedule schedule = getOrCreateSchedule(agentId);
        schedule.setMaxOrdersPerDay(max);
        scheduleRepo.save(schedule);
    }
}
