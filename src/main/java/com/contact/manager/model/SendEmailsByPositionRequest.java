// src/main/java/com/contact/manager/controllers/SendEmailsRequest.java
package com.contact.manager.model;

import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

public class SendEmailsByPositionRequest {
    private Boolean markForInterview;
    @NotBlank
    private String subject;
    @NotBlank
    private String templateName;

    private List<Long> candidates = new ArrayList<>();

    // Getters and setters
    public Boolean getMarkForInterview() {
        return markForInterview;
    }

    public void setMarkForInterview(Boolean markForInterview) {
        this.markForInterview = markForInterview;
    }

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

    public List<Long> getCandidates() {
        return candidates;
    }

    public SendEmailsByPositionRequest setCandidates(List<Long> candidates) {
        this.candidates = candidates;
        return this;
    }
}