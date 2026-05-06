package com.example.UC_Backend.schedule;

import java.io.Serializable;

/**
 * A single recurring availability window within a week.
 * Stored as embedded document inside AgentSchedule.
 *
 * Example: { dayOfWeek: "MONDAY", startHour: 9, endHour: 18 }
 * = "Available every Monday from 9am to 6pm"
 */
public class AvailabilitySlot implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Day of week: MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY */
    private String dayOfWeek;

    /** 0–23 (24h format) */
    private int startHour;

    /** 0–23 */
    private int endHour;

    public AvailabilitySlot() {}

    public AvailabilitySlot(String dayOfWeek, int startHour, int endHour) {
        this.dayOfWeek = dayOfWeek;
        this.startHour = startHour;
        this.endHour   = endHour;
    }

    public String getDayOfWeek()       { return dayOfWeek; }
    public void setDayOfWeek(String d) { this.dayOfWeek = d; }
    public int getStartHour()          { return startHour; }
    public void setStartHour(int h)    { this.startHour = h; }
    public int getEndHour()            { return endHour; }
    public void setEndHour(int h)      { this.endHour = h; }
}
