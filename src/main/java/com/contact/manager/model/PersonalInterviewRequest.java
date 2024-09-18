// src/main/java/com/contact/manager/controllers/ScheduleInterviewRequest.java
package com.contact.manager.model;


import com.contact.manager.services.scheduler.LocalDateTimeRange;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class PersonalInterviewRequest implements LocalDateTimeRange {
    @NotBlank
    private String subject;
    @NotBlank
    private String templateName;
    @NotNull
    private LocalDateTime startDateTime;
    @NotNull
    private LocalDateTime endDateTime;

    private boolean sendEmails;

    public @NotBlank String getSubject() {
        return subject;
    }

    public PersonalInterviewRequest setSubject(@NotBlank String subject) {
        this.subject = subject;
        return this;
    }

    public @NotBlank String getTemplateName() {
        return templateName;
    }

    public PersonalInterviewRequest setTemplateName(@NotBlank String templateName) {
        this.templateName = templateName;
        return this;
    }

    @Override
    public @NotNull LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public PersonalInterviewRequest setStartDateTime(@NotNull LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
        return this;
    }

    @Override
    public @NotNull LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public PersonalInterviewRequest setEndDateTime(@NotNull LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
        return this;
    }

    public boolean isSendEmails() {
        return sendEmails;
    }

    public PersonalInterviewRequest setSendEmails(boolean sendEmails) {
        this.sendEmails = sendEmails;
        return this;
    }
}