package com.contact.manager.model;

import jakarta.validation.constraints.NotNull;

public class MarkForInterviewRequest {
    @NotNull
    private Boolean markForInterview;

    public boolean isMarkForInterview() {
        return markForInterview;
    }

    public void setMarkForInterview(boolean markForInterview) {
        this.markForInterview = markForInterview;
    }
}
