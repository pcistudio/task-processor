// src/main/java/com/contact/manager/services/scheduler/SchedulerContext.java
package com.contact.manager.services.scheduler;

import org.springframework.util.Assert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Iterator;

public class SchedulerContext {
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate startDate;
    private int meetingDurationMinutes;
    private int timeBetweenMeetings;
    private boolean sendEmails;


    private SchedulerContext(Builder builder) {
        this.startTime = builder.startTime;
        this.endTime = builder.endTime;
        this.startDate = builder.startDate;
        this.meetingDurationMinutes = builder.interviewDurationMinutes;
        this.timeBetweenMeetings = builder.timeBetweenInterviews;
        this.sendEmails = builder.sendEmails;
    }

    public static class Builder {
        private LocalTime startTime;
        private LocalTime endTime;
        private LocalDate startDate;
        private int interviewDurationMinutes;
        private int timeBetweenInterviews;
        private boolean sendEmails = true;

        public Builder startTime(LocalTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder endTime(LocalTime endTime) {
            this.endTime = endTime;
            return this;
        }

        public Builder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder interviewDurationMinutes(int interviewDurationMinutes) {
            this.interviewDurationMinutes = interviewDurationMinutes;
            return this;
        }

        public Builder timeBetweenInterviews(int timeBetweenInterviews) {
            this.timeBetweenInterviews = timeBetweenInterviews;
            return this;
        }

        public Builder sendEmails(boolean sendEmails) {
            this.sendEmails = sendEmails;
            return this;
        }

        public SchedulerContext build() {
            Assert.isTrue(!startTime.isAfter(endTime.minusMinutes(interviewDurationMinutes)), "No interview can be scheduled within the given time frame");
            return new SchedulerContext(this);
        }
    }

//    private class LocalDateTimeRangeImpl implements LocalDateTimeRange {
//
//        @Override
//        public LocalDateTime getStartDateTime() {
//            return startDate.atTime(startTime);
//        }
//
//        @Override
//        public LocalDateTime getEndDateTime() {
//            return startDate.atTime(endTime);
//        }
//    }
//
//    public LocalDateTimeRange toLocalDateTimeRange() {
//        return new LocalDateTimeRangeImpl();
//    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public int getMeetingDurationMinutes() {
        return meetingDurationMinutes;
    }

    public int getTimeBetweenMeetings() {
        return timeBetweenMeetings;
    }

    public boolean shouldSendEmails() {
        return sendEmails;
    }
}