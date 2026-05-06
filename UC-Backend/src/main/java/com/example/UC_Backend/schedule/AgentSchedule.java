package com.example.UC_Backend.schedule;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Agent weekly availability schedule.
 * Stored as one document per agent (agentId is unique).
 *
 * Design decision: Store weekly recurrence slots + one-off blocked dates
 * separately. This mirrors real-world scheduling (e.g. "I work Mon–Fri 9–6,
 * except next Tuesday I'm unavailable").
 *
 * Future extension: replace with iCalendar (RFC 5545) for calendar app sync.
 */
@Document(collection = "agent_schedules")
public class AgentSchedule implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id = UUID.randomUUID().toString();

    @Indexed(unique = true)
    private int agentId;

    /**
     * Weekly recurring availability slots.
     * e.g. [{ dayOfWeek: "MONDAY", startHour: 9, endHour: 18 }]
     */
    private List<AvailabilitySlot> weeklySlots = new ArrayList<>();

    /**
     * Specific dates the agent is NOT available (vacation, emergency, etc.)
     * e.g. ["2025-12-25", "2025-01-26"]
     */
    private List<LocalDate> blockedDates = new ArrayList<>();

    /** Default: accept all orders (no schedule restrictions) */
    private boolean scheduleRestrictionEnabled = false;

    /** Maximum orders the agent accepts per day (0 = unlimited) */
    private int maxOrdersPerDay = 0;

    public AgentSchedule() {}

    public AgentSchedule(int agentId) {
        this.agentId = agentId;
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────

    public String getId()                                        { return id; }
    public int getAgentId()                                      { return agentId; }
    public List<AvailabilitySlot> getWeeklySlots()               { return weeklySlots; }
    public void setWeeklySlots(List<AvailabilitySlot> slots)     { this.weeklySlots = slots; }
    public List<LocalDate> getBlockedDates()                     { return blockedDates; }
    public void setBlockedDates(List<LocalDate> dates)           { this.blockedDates = dates; }
    public boolean isScheduleRestrictionEnabled()                { return scheduleRestrictionEnabled; }
    public void setScheduleRestrictionEnabled(boolean enabled)   { this.scheduleRestrictionEnabled = enabled; }
    public int getMaxOrdersPerDay()                              { return maxOrdersPerDay; }
    public void setMaxOrdersPerDay(int max)                      { this.maxOrdersPerDay = max; }
}
