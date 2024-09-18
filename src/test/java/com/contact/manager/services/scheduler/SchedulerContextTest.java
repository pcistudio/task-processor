package com.contact.manager.services.scheduler;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class SchedulerContextTest {
    @Test
    void testInvalidTimeFrame() {
        SchedulerContext.Builder builder = new SchedulerContext.Builder()
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(10, 30))
                .startDate(LocalDate.now())
                .interviewDurationMinutes(45)
                .timeBetweenInterviews(15);
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void testValidTimeFrame() {
        SchedulerContext schedulerContext = new SchedulerContext.Builder()
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(10, 30))
                .startDate(LocalDate.now())
                .interviewDurationMinutes(30)
                .timeBetweenInterviews(0)
                .build();
        assertEquals(LocalTime.of(10, 0), schedulerContext.getStartTime());

    }

    @Test
    void testInvalidTimeFrame2() {
        SchedulerContext.Builder builder = new SchedulerContext.Builder()
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(10, 30))
                .startDate(LocalDate.now())
                .interviewDurationMinutes(31)
                .timeBetweenInterviews(0);
        assertThrows(IllegalArgumentException.class, builder::build);

    }


}