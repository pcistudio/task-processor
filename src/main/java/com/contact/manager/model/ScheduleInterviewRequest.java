// src/main/java/com/contact/manager/controllers/ScheduleInterviewRequest.java
package com.contact.manager.model;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public class ScheduleInterviewRequest {
    @NotBlank
    private String subject;
    @NotBlank
    private String templateName;
    @NotNull
    private LocalTime startTime;
    @NotNull
    private LocalTime endTime;
    @NotNull
    private LocalDate startDate;
    @NotNull
    private Integer interviewDurationMinutes;
    private int timeBetweenInterviews;
    private boolean sendEmails;

    // Getters and setters
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public @NotNull LocalTime getStartTime() {
        return startTime;
    }

    public ScheduleInterviewRequest setStartTime(@NotNull LocalTime startTime) {
        this.startTime = startTime;
        return this;
    }

    public @NotNull LocalTime getEndTime() {
        return endTime;
    }

    public ScheduleInterviewRequest setEndTime(@NotNull LocalTime endTime) {
        this.endTime = endTime;
        return this;
    }

    public @NotNull LocalDate getStartDate() {
        return startDate;
    }

    public ScheduleInterviewRequest setStartDate(@NotNull LocalDate startDate) {
        this.startDate = startDate;
        return this;
    }

    public ScheduleInterviewRequest setInterviewDurationMinutes(@NotNull Integer interviewDurationMinutes) {
        this.interviewDurationMinutes = interviewDurationMinutes;
        return this;
    }

    public int getInterviewDurationMinutes() {
        return interviewDurationMinutes;
    }

    public void setInterviewDurationMinutes(int interviewDurationMinutes) {
        this.interviewDurationMinutes = interviewDurationMinutes;
    }

    public int getTimeBetweenInterviews() {
        return timeBetweenInterviews;
    }

    public void setTimeBetweenInterviews(int timeBetweenInterviews) {
        this.timeBetweenInterviews = timeBetweenInterviews;
    }

    public boolean getSendEmails() {
        return sendEmails;
    }

    public void setSendEmails(boolean sendEmails) {
        this.sendEmails = sendEmails;
    }
}