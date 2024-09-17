package com.contact.manager.model;

import jakarta.validation.constraints.NotBlank;

public class NoteRequest {
    @NotBlank
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}