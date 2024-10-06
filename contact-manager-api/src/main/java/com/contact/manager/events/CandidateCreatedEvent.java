// src/main/java/com/contact/manager/events/CandidateCreatedEvent.java
package com.contact.manager.events;

import com.contact.manager.entities.Candidate;
import org.springframework.context.ApplicationEvent;

public class CandidateCreatedEvent extends ApplicationEvent {
    private final Candidate candidate;

    public CandidateCreatedEvent(Object source, Candidate candidate) {
        super(source);
        this.candidate = candidate;
    }

    public Candidate getCandidate() {
        return candidate;
    }
}