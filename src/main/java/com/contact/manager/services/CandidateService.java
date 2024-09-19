// src/main/java/com/contact/manager/service/CandidateService.java
package com.contact.manager.services;

import com.contact.manager.entities.Candidate;
import com.contact.manager.entities.Contact;
import com.contact.manager.model.AttachmentResource;
import com.contact.manager.services.scheduler.LocalDateTimeRange;
import com.contact.manager.services.scheduler.ScheduleMeeting;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CandidateService {
    List<Candidate> getAllCandidates();

    Candidate getCandidateById(Long id);

    Candidate createCandidate(Candidate candidate);

    Candidate updateCandidate(Long id, Candidate candidate);

    Candidate addCandidateNote(Long candidateId, String note);

    boolean deleteCandidate(Long id);

    Candidate addAttachment(Long id, MultipartFile file);

    List<Candidate> searchCandidates(String filter);

    AttachmentResource loadAttachmentAsResource(long candidateId, int index);

    Contact convertToContact(Long candidateId);

    Candidate assignPosition(Long candidateId, Long positionId);

    ScheduleMeeting scheduleInterview(Long candidateId, String subject, String templateName, LocalDateTimeRange range);
}