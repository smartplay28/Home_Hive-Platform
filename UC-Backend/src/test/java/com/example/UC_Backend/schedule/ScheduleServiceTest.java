package com.example.UC_Backend.schedule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ScheduleService.isAvailableNow() — the core method wired into
 * agent matching. These tests simulate different times of day and day-of-week
 * to validate the availability logic exhaustively.
 *
 * Why thorough tests here?
 *   isAvailableNow() is called on EVERY agent for EVERY order.
 *   A bug here directly impacts revenue (agents miss orders) or customer experience.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduleService Unit Tests")
class ScheduleServiceTest {

    @Mock ScheduleRepository scheduleRepo;
    @InjectMocks ScheduleService scheduleService;

    private static final int AGENT_ID = 42;

    // ─── No schedule = always available (backward compat) ─────────────────────

    @Nested
    @DisplayName("No Schedule (Default Behavior)")
    class NoSchedule {

        @Test
        @DisplayName("Agent with no schedule is always available")
        void noSchedule_alwaysAvailable() {
            when(scheduleRepo.findByAgentId(AGENT_ID)).thenReturn(Optional.empty());
            assertThat(scheduleService.isAvailableNow(AGENT_ID)).isTrue();
        }

        @Test
        @DisplayName("Agent with schedule restrictions disabled is always available")
        void scheduleDisabled_alwaysAvailable() {
            AgentSchedule schedule = new AgentSchedule(AGENT_ID);
            schedule.setScheduleRestrictionEnabled(false);
            when(scheduleRepo.findByAgentId(AGENT_ID)).thenReturn(Optional.of(schedule));
            assertThat(scheduleService.isAvailableNow(AGENT_ID)).isTrue();
        }
    }

    // ─── Blocked dates ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Blocked Dates")
    class BlockedDates {

        @Test
        @DisplayName("Agent on a blocked date is unavailable")
        void blockedToday_notAvailable() {
            AgentSchedule schedule = new AgentSchedule(AGENT_ID);
            schedule.setScheduleRestrictionEnabled(true);
            schedule.setBlockedDates(List.of(LocalDate.now())); // today is blocked
            // Needs a slot for today that would normally pass
            String today = DayOfWeek.from(LocalDate.now()).name();
            schedule.setWeeklySlots(List.of(new AvailabilitySlot(today, 0, 23)));

            when(scheduleRepo.findByAgentId(AGENT_ID)).thenReturn(Optional.of(schedule));

            assertThat(scheduleService.isAvailableNow(AGENT_ID)).isFalse();
        }

        @Test
        @DisplayName("Agent with blocked date in future is available today")
        void blockedFutureDate_availableToday() {
            AgentSchedule schedule = new AgentSchedule(AGENT_ID);
            schedule.setScheduleRestrictionEnabled(true);
            schedule.setBlockedDates(List.of(LocalDate.now().plusDays(7))); // next week blocked
            String today = DayOfWeek.from(LocalDate.now()).name();
            schedule.setWeeklySlots(List.of(new AvailabilitySlot(today, 0, 23)));

            when(scheduleRepo.findByAgentId(AGENT_ID)).thenReturn(Optional.of(schedule));

            assertThat(scheduleService.isAvailableNow(AGENT_ID)).isTrue();
        }
    }

    // ─── Weekly slots ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Weekly Slot Matching")
    class WeeklySlots {

        @Test
        @DisplayName("Agent with no slots for today is unavailable")
        void noSlotForToday_notAvailable() {
            AgentSchedule schedule = new AgentSchedule(AGENT_ID);
            schedule.setScheduleRestrictionEnabled(true);
            // Only add a slot for a day that is NOT today
            String notToday = DayOfWeek.from(LocalDate.now()).plus(1).name();
            schedule.setWeeklySlots(List.of(new AvailabilitySlot(notToday, 9, 18)));

            when(scheduleRepo.findByAgentId(AGENT_ID)).thenReturn(Optional.of(schedule));

            assertThat(scheduleService.isAvailableNow(AGENT_ID)).isFalse();
        }

        @Test
        @DisplayName("Agent with all-day slot (0-23) on today is available")
        void allDaySlot_available() {
            AgentSchedule schedule = new AgentSchedule(AGENT_ID);
            schedule.setScheduleRestrictionEnabled(true);
            String today = DayOfWeek.from(LocalDate.now()).name();
            schedule.setWeeklySlots(List.of(new AvailabilitySlot(today, 0, 23)));

            when(scheduleRepo.findByAgentId(AGENT_ID)).thenReturn(Optional.of(schedule));

            assertThat(scheduleService.isAvailableNow(AGENT_ID)).isTrue();
        }
    }

    // ─── getOrCreateSchedule ──────────────────────────────────────────────────

    @Test
    @DisplayName("getOrCreateSchedule creates new schedule for new agent")
    void getOrCreate_newAgent_createsDefaultSchedule() {
        when(scheduleRepo.findByAgentId(AGENT_ID)).thenReturn(Optional.empty());
        when(scheduleRepo.save(any(AgentSchedule.class)))
                .thenAnswer(i -> i.getArgument(0));

        AgentSchedule result = scheduleService.getOrCreateSchedule(AGENT_ID);

        assertThat(result).isNotNull();
        assertThat(result.isScheduleRestrictionEnabled()).isFalse(); // default = unrestricted
        verify(scheduleRepo).save(any(AgentSchedule.class));
    }
}
