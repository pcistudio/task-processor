// src/main/java/com/contact/manager/converter/CandidateToContactConverter.java
package com.contact.manager.model.converter;

import com.contact.manager.entities.Candidate;
import com.contact.manager.entities.Contact;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;

@Component
public class CandidateToContactConverter {

    public Contact convert(Candidate candidate) {
        Contact contact = new Contact();
        contact.setFirstName(candidate.getFirstName());
        contact.setLastName(candidate.getLastName());
        contact.setEmail(candidate.getEmail());
        contact.setJobTitle(candidate.getJobTitle());
        contact.setOfficePhone(candidate.getOfficePhone());
        contact.setMobile(candidate.getMobile());
        contact.setDescription(candidate.getDescription());

        contact.setPrimaryAddress(candidate.getPrimaryAddress());
        contact.setSecondaryAddress(candidate.getSecondaryAddress());

        contact.setNotes(new ArrayList<>(candidate.getNotes()));
        contact.setAttachments(new HashSet<>(candidate.getAttachments()));
        return contact;
    }
}