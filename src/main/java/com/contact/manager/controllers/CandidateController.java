// src/main/java/com/contact/manager/controller/CandidateController.java
package com.contact.manager.controllers;

import com.contact.manager.entities.Candidate;
import com.contact.manager.entities.Contact;
import com.contact.manager.model.AttachmentResource;
import com.contact.manager.model.CandidateModel;
import com.contact.manager.model.CandidateView;
import com.contact.manager.model.ContactModel;
import com.contact.manager.services.CandidateService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/candidates")
public class CandidateController {

    private static final Logger log = LoggerFactory.getLogger(CandidateController.class);
    private final CandidateService candidateService;

    public CandidateController(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    @GetMapping
    public ResponseEntity<List<CandidateView>> getAllCandidates(@RequestParam(required = false) String filter) {
        List<Candidate> contacts = StringUtils.hasLength(filter) ? candidateService.searchCandidates(filter) : candidateService.getAllCandidates();
        return ResponseEntity.ok(
                contacts.stream()
                        .map(CandidateView::new)
                        .toList()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<CandidateModel> getCandidateById(@PathVariable Long id) {
        Candidate candidate = candidateService.getCandidateById(id);
        if (candidate != null) {
            return ResponseEntity.ok(CandidateModel.fromCandidate(candidate));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public CandidateModel createCandidate(@RequestBody @Valid Candidate candidate) {
        return CandidateModel.fromCandidate(candidateService.createCandidate(candidate));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CandidateModel> updateCandidate(@PathVariable Long id, @RequestBody Candidate candidate) {
        Candidate updatedCandidate = candidateService.updateCandidate(id, candidate);
        if (updatedCandidate != null) {
            return ResponseEntity.ok(CandidateModel.fromCandidate(updatedCandidate));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCandidate(@PathVariable Long id) {
        if (candidateService.deleteCandidate(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/attachments")
    public ResponseEntity<CandidateModel> addAttachment(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        Candidate candidate = candidateService.addAttachment(id, file);
        return ResponseEntity.ok(CandidateModel.fromCandidate(candidate));
    }

    @GetMapping("/{candidateId}/attachments/{index}")
    public ResponseEntity<Resource> serveFile(@PathVariable long candidateId, @PathVariable int index) {
        try {
            AttachmentResource attachmentResource = candidateService.loadAttachmentAsResource(candidateId, index);
            log.info("Downloading attachment: {}", attachmentResource.getAttachment().getFileName());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachmentResource.getAttachment().getFileName() + "\"")
                    .body(attachmentResource.getResource());
        } catch (IllegalArgumentException ex) {
            log.error("Attachment not found candidateId={}, index={}", candidateId, index, ex);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{candidateId}:convert-to-contact")
    public ContactModel convertToContact(@PathVariable Long candidateId) {
        Contact contact =  candidateService.convertToContact(candidateId);
        return ContactModel.fromContact(contact);
    }
}