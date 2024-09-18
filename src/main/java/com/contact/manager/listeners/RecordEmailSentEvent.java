// src/main/java/com/contact/manager/listeners/CandidateCreatedEventListener.java
package com.contact.manager.listeners;

import com.contact.manager.entities.Note;
import com.contact.manager.events.PersonEmailSentEvent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Component
public class RecordEmailSentEvent {

    private static final Logger log = LoggerFactory.getLogger(RecordEmailSentEvent.class);

    @PersistenceContext
    private EntityManager entityManager;

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleCandidateCreatedEvent(PersonEmailSentEvent event) {
        log.debug("Recording email sent event for person {}", event.getPerson().getEmail());
        if (event.getPerson() == null) {
            log.warn("Person is null, skipped event recording");
            return;
        }

        if (event.getPerson().anonymous()) {
            log.warn("Person is anonymous, skipped event recording");
            return;
        }

        Assert.notNull(event.getPerson().getId(), "Person id is null");

        entityManager.refresh(event.getPerson());
        event.getPerson()
                .getNotes()
                .add(crateNote(event));
        entityManager.persist(event.getPerson());
        log.info("Email sent event recorded for {}={}", event.getPerson().getClass().getSimpleName(), event.getPerson().getId());
    }

    private static Note crateNote(PersonEmailSentEvent event) {
        return new Note()
                .setContent(getContent(event));
    }

    private static String getContent(PersonEmailSentEvent event) {
        return String.format("Email %s : %s",
                event.getStatus() ? "sent" : "not sent",
                event.getEmailText()
        );
    }


}