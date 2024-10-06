// src/main/java/com/contact/manager/model/AddNoteRequest.java
package com.contact.manager.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AddNoteRequest {
    @NotBlank
    @Size(max = 1024)
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}