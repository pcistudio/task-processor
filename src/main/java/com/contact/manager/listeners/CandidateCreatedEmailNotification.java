// src/main/java/com/contact/manager/listeners/CandidateCreatedEventListener.java
package com.contact.manager.listeners;

import com.contact.manager.events.CandidateCreatedEvent;
import com.contact.manager.services.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CandidateCreatedEmailNotification {

    private static final Logger log = LoggerFactory.getLogger(CandidateCreatedEmailNotification.class);
    private final MailService mailService;

    public CandidateCreatedEmailNotification(MailService mailService) {
        this.mailService = mailService;
    }

    @EventListener
    public void handleCandidateCreatedEvent(CandidateCreatedEvent event) {
        log.debug("Sending email notification for candidate {}", event.getCandidate().getEmail());
        mailService.sendEmailWithTemplate(
                event.getCandidate(),
                "Candidatura recibida",
                "candidate-created-email"
        );
    }
}