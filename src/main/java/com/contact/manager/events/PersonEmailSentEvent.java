// src/main/java/com/contact/manager/events/EmailSentEvent.java
package com.contact.manager.events;

import com.contact.manager.entities.Person;
import org.springframework.context.ApplicationEvent;

public class PersonEmailSentEvent extends ApplicationEvent {
    private final Person person;
    private final String subject;
    private final String emailText;
    private final boolean status;

    public PersonEmailSentEvent(Object source, Person person, String subject, String emailText, boolean status) {
        super(source);
        this.person = person;
        this.subject = subject;
        this.emailText = emailText;
        this.status = status;
    }

    public Person getPerson() {
        return person;
    }

    public String getSubject() {
        return subject;
    }

    public String getEmailText() {
        return emailText;
    }

    public boolean getStatus() {
        return status;
    }
}