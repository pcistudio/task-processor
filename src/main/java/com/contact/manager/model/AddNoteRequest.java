// src/main/java/com/contact/manager/model/AddNoteRequest.java
package com.contact.manager.model;

import jakarta.validation.constraints.NotBlank;

public class AddNoteRequest {
    @NotBlank
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}