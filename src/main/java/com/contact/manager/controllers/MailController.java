// src/main/java/com/contact/manager/controller/MailController.java
package com.contact.manager.controllers;

import com.contact.manager.entities.Candidate;
import com.contact.manager.events.CandidateCreatedEvent;
import com.contact.manager.listeners.CandidateCreatedEmailNotification;
import com.contact.manager.services.MailService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mail")
public class MailController {

    private final MailService mailService;
    private final CandidateCreatedEmailNotification candidateCreatedEmailNotification;


    public MailController(MailService mailService, CandidateCreatedEmailNotification candidateCreatedEmailNotification) {
        this.mailService = mailService;
        this.candidateCreatedEmailNotification = candidateCreatedEmailNotification;
    }

    @PostMapping("/send-test-email")
    public String sendTestEmail(@RequestParam String to) {
        mailService.sendEmail(to, "Test Email", "This is a test email.");
        return "Test email sent to " + to;
    }

    @PostMapping("/send-candidate-created-email")
    public String sendCandidateCreatedEmail(@RequestParam String to, @RequestParam String firstName, @RequestParam String lastName) {
        Candidate candidate = new Candidate();
        candidate.setEmail(to);
        candidate.setFirstName(firstName);
        candidate.setLastName(lastName);

        CandidateCreatedEvent event = new CandidateCreatedEvent(this, candidate);
        candidateCreatedEmailNotification.handleCandidateCreatedEvent(event);

        return "Candidate created email sent to " + to;
    }
}