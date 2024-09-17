// src/main/java/com/contact/manager/service/CandidateServiceImpl.java
package com.contact.manager.services;

import com.contact.manager.entities.Attachment;
import com.contact.manager.entities.Candidate;
import com.contact.manager.entities.Contact;
import com.contact.manager.events.CandidateCreatedEvent;
import com.contact.manager.model.AttachmentResource;
import com.contact.manager.model.converter.CandidateToContactConverter;
import com.contact.manager.repositories.CandidateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Service
public class CandidateServiceImpl implements CandidateService {

    private static final Logger log = LoggerFactory.getLogger(CandidateServiceImpl.class);
    private final CandidateRepository candidateRepository;

    private final AttachmentManager attachmentManager;

    private final CandidateToContactConverter candidateToContactConverter;

    private final ContactService contactService;

    private final ApplicationEventPublisher eventPublisher;



    public CandidateServiceImpl(CandidateRepository candidateRepository, AttachmentManager attachmentManager, CandidateToContactConverter candidateToContactConverter, ContactService contactService, ApplicationEventPublisher eventPublisher) {
        this.candidateRepository = candidateRepository;
        this.attachmentManager = attachmentManager;
        this.candidateToContactConverter = candidateToContactConverter;
        this.contactService = contactService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public List<Candidate> getAllCandidates() {
        return candidateRepository.findAll();
    }

    @Override
    public Candidate getCandidateById(Long id) {
        Optional<Candidate> candidate = candidateRepository.findById(id);
        return candidate.orElse(null);
    }

    @Override
    public Candidate createCandidate(Candidate candidate) {
        Assert.isNull(candidate.getId(), "Candidate id must  be null");
        Candidate savedCandidate = candidateRepository.save(candidate);
        eventPublisher.publishEvent(new CandidateCreatedEvent(this, savedCandidate));
        return savedCandidate;
    }

    @Override
    public Candidate updateCandidate(Long id, Candidate candidate) {
        Candidate candidateStored = candidateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found"));

        candidate.setId(id);
        candidate.setAttachments(candidateStored.getAttachments());
        return candidateRepository.save(candidate);
    }

    @Override
    public boolean deleteCandidate(Long id) {
        if (candidateRepository.existsById(id)) {
            candidateRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Candidate addAttachment(Long candidateId, MultipartFile file) {

        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found"));

        Path path = attachmentManager.storeAttachment(file);
        try {
            Attachment attachment = new Attachment();
            attachment.setId((long)candidate.getAttachments().size());
            attachment.setFileName(file.getOriginalFilename());
            attachment.setFileType(file.getContentType());
            attachment.setFilePath(path.toAbsolutePath().toString());
            candidate.getAttachments().add(attachment);
            // Save file path to database
            return candidateRepository.save(candidate);
        } catch (Exception e) {
            attachmentManager.deleteAttachment(path);
            throw new IllegalArgumentException("Could not save attachment", e);
        }
    }

    @Override
    public List<Candidate> searchCandidates(String filter) {
        return candidateRepository.findByFirstNameContainingOrLastNameContaining(filter, filter);
    }

    @Override
    public AttachmentResource loadAttachmentAsResource(long candidateId, int index) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found"));
        if (index < 0 || index >= candidate.getAttachments().size()) {
            throw new IllegalArgumentException("Attachment not found");
        }
        Attachment attachment = candidate.getAttachments().get(index);
        Resource resource = attachmentManager.loadAttachmentAsResource(attachment.getFilePath());

        return new AttachmentResource()
                .setAttachment(attachment)
                .setResource(resource);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Contact convertToContact(Long candidateId) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found"));
        Contact contact = candidateToContactConverter.convert(candidate);
        contactService.saveContact(contact);
        candidateRepository.deleteById(candidateId);
        log.info("Candidate {} converted to contact {}", candidateId, contact.getId());
        return contact;
    }

//
//    @Override
//    public boolean deleteAttachment(Long candidateId, Long attachmentId) {
//        Candidate candidate = candidateRepository.findById(candidateId)
//                .orElseThrow(() -> new IllegalArgumentException("Candidate not found"));
//
//        Attachment attachment = candidate.getAttachments().stream()
//                .filter(a -> a.getId().equals(attachmentId))
//                .findFirst()
//                .orElse(null);
//
//        if(attachment == null) {
//            return false;
//        }
//
//        candidate.getAttachments().remove(attachment);
//        candidateRepository.save(candidate);
//        attachmentManager.deleteAttachment(attachment.getFilePath());
//
//        return true;
//    }
}