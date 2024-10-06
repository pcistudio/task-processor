package com.contact.manager.model;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

public class ErrorResponse {
    private final LocalDateTime timestamp;
    private final String message;
    private final Set<String> details;

    public ErrorResponse(LocalDateTime timestamp, String message, String details) {
        this.timestamp = timestamp;
        this.message = message;
        this.details = Set.of(details);
    }

    public ErrorResponse(LocalDateTime timestamp, String message, Set<String> details) {
        this.timestamp = timestamp;
        this.message = message;
        this.details = Collections.unmodifiableSet(details);
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public Set<String> getDetails() {
        return details;
    }
}