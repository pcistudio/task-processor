// src/main/java/com/contact/manager/service/CandidateServiceImpl.java
package com.contact.manager.services;

import com.contact.manager.entities.*;
import com.contact.manager.events.CandidateCreatedEvent;
import com.contact.manager.model.AttachmentResource;
import com.contact.manager.model.converter.CandidateToContactConverter;
import com.contact.manager.repositories.CandidateRepository;
import com.contact.manager.repositories.PositionRepository;
import com.contact.manager.services.scheduler.LocalDateTimeRange;
import com.contact.manager.services.scheduler.MeetingInfo;
import com.contact.manager.services.scheduler.MeetingScheduler;
import com.contact.manager.services.scheduler.ScheduleMeeting;
import jakarta.persistence.EntityNotFoundException;
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
import java.util.Objects;
import java.util.Optional;

@Service
public class CandidateServiceImpl implements CandidateService {

    private static final Logger log = LoggerFactory.getLogger(CandidateServiceImpl.class);
    public static final String CANDIDATE_NOT_FOUND = "Candidate not found";
    private final CandidateRepository candidateRepository;

    private final AttachmentManager attachmentManager;

    private final CandidateToContactConverter candidateToContactConverter;

    private final ContactService contactService;

    private final ApplicationEventPublisher eventPublisher;

    private final PositionRepository positionRepository;

    private final MeetingScheduler meetingScheduler;


    public CandidateServiceImpl(CandidateRepository candidateRepository, AttachmentManager attachmentManager, CandidateToContactConverter candidateToContactConverter, ContactService contactService, ApplicationEventPublisher eventPublisher, PositionRepository positionRepository, MeetingScheduler meetingScheduler) {
        this.candidateRepository = candidateRepository;
        this.attachmentManager = attachmentManager;
        this.candidateToContactConverter = candidateToContactConverter;
        this.contactService = contactService;
        this.eventPublisher = eventPublisher;
        this.positionRepository = positionRepository;
        this.meetingScheduler = meetingScheduler;
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
                .orElseThrow(() -> new IllegalArgumentException(CANDIDATE_NOT_FOUND));

        candidate.setId(id);
        candidate.setAttachments(candidateStored.getAttachments());
        candidate.setNotes(candidateStored.getNotes());
        return candidateRepository.save(candidate);
    }

    @Override
    public Candidate addCandidateNote(Long candidateId, String note) {
        Candidate candidate = getCandidateById(candidateId);
        if (candidate != null) {
            candidate.getNotes().add(new Note().setContent(note));
            return candidateRepository.save(candidate);
        } else {
            throw new EntityNotFoundException(CANDIDATE_NOT_FOUND);
        }
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
                .orElseThrow(() -> new IllegalArgumentException(CANDIDATE_NOT_FOUND));

        Path path = attachmentManager.storeAttachment(file);
        try {
            Attachment attachment = new Attachment();
            attachment.setId((long)candidate.getAttachments().size());
            attachment.setFileName(file.getOriginalFilename());
            attachment.setFileType(file.getContentType());
            attachment.setFilePath(path.toAbsolutePath().toString());
            candidate.getAttachments().add(attachment);
            // Save file path to database
            Candidate saveCandidate = candidateRepository.save(candidate);
            log.info("Attachment={} added to candidate={} ", attachment.getFilePath(), saveCandidate.getId());
            return saveCandidate;
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
                .orElseThrow(() -> new IllegalArgumentException(CANDIDATE_NOT_FOUND));
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
                .orElseThrow(() -> new IllegalArgumentException(CANDIDATE_NOT_FOUND));
        Contact contact = candidateToContactConverter.convert(candidate);
        contactService.saveContact(contact);
        candidateRepository.deleteById(candidateId);
        log.info("Candidate {} converted to contact {}", candidateId, contact.getId());
        return contact;
    }

    @Override
    public Candidate assignPosition(Long candidateId, Long positionId) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException(CANDIDATE_NOT_FOUND));
        Position position = positionRepository.findById(positionId)
                .orElseThrow(() -> new IllegalArgumentException("Position not found"));
        candidate.setPosition(position);
        return candidateRepository.save(candidate);
    }

    @Override
    public ScheduleMeeting scheduleInterview(Long candidateId, String subject, String templateName, LocalDateTimeRange range) {
        // Existing logic to get candidates and mark them for interview
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("Position not found"));

        // Create MeetingInfo
        MeetingInfo.MeetingInfoBuilder meetingInfoBuilder = MeetingInfo.builder()
                .persons(List.of(candidate))
                .position(candidate.getPosition())
                .subject(Objects.requireNonNullElseGet(subject, () -> candidate.getPosition().getTitle() + " Interview"))
                .templateName(templateName)
                ;

        return meetingScheduler.scheduleMeeting(meetingInfoBuilder, range);
    }

    @Override
    public void markForInterview(Long candidateId, boolean markForInterview) {
        candidateRepository.updateMarkForInterview(candidateId, markForInterview);
    }
}