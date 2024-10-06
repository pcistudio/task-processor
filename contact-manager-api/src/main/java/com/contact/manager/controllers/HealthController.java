// src/main/java/com/contact/manager/controller/MailController.java
package com.contact.manager.controllers;

import com.contact.manager.entities.Candidate;
import com.contact.manager.events.CandidateCreatedEvent;
import com.contact.manager.listeners.CandidateCreatedEmailNotification;
import com.contact.manager.services.AttachmentManager;
import com.contact.manager.services.MailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final MailService mailService;
    private final CandidateCreatedEmailNotification candidateCreatedEmailNotification;
    private final AttachmentManager attachmentManager;


    public HealthController(MailService mailService, CandidateCreatedEmailNotification candidateCreatedEmailNotification, AttachmentManager attachmentManager) {
        this.mailService = mailService;
        this.candidateCreatedEmailNotification = candidateCreatedEmailNotification;
        this.attachmentManager = attachmentManager;
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

    @GetMapping("/canUpload")
    public ResponseEntity<Boolean> testUploadDirectory() {
        return ResponseEntity.ok(attachmentManager.canWrite());
    }

    @PostMapping("/writeToUpload")
    public ResponseEntity<String> testWriteUpload() throws IOException {
        Path path = attachmentManager.storeAttachment(new ByteArrayInputStream("test".getBytes()), ".test");
        return ResponseEntity.ok(path.toAbsolutePath().toString());
    }
}