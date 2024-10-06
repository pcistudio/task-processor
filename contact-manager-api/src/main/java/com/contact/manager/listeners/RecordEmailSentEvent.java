// src/main/java/com/contact/manager/listeners/CandidateCreatedEventListener.java
package com.contact.manager.listeners;

import com.contact.manager.entities.Note;
import com.contact.manager.entities.Person;
import com.contact.manager.events.PersonEmailSentEvent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RecordEmailSentEvent {

    private static final Logger log = LoggerFactory.getLogger(RecordEmailSentEvent.class);

    @PersistenceContext
    private EntityManager entityManager;

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleCandidateCreatedEvent(PersonEmailSentEvent event) {
        Person person = event.getPerson();

        if (person == null) {
            log.warn("Person is null, skipped event recording");
            return;
        }
        log.debug("Recording email sent event for person {}", person.getEmail());

        if (person.anonymous()) {
            log.warn("Person is anonymous, skipped event recording");
            return;
        }

        Assert.notNull(person.getId(), "Person id is null");
        person.getNotes()
                .removeIf(note -> StringUtils.isBlank(note.getContent()));
        person.getNotes().add(crateNote(event));
        entityManager.merge(person);
//        entityManager.persist(person);
        log.info("Email sent event recorded for {}={}", person.getClass().getSimpleName(), person.getId());
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