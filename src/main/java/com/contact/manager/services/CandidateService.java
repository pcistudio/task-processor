// src/main/java/com/contact/manager/service/CandidateService.java
package com.contact.manager.services;

import com.contact.manager.entities.Candidate;
import com.contact.manager.entities.Contact;
import com.contact.manager.model.AttachmentResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CandidateService {
    List<Candidate> getAllCandidates();

    Candidate getCandidateById(Long id);

    Candidate createCandidate(Candidate candidate);

    Candidate updateCandidate(Long id, Candidate candidate);

    boolean deleteCandidate(Long id);

    Candidate addAttachment(Long id, MultipartFile file);

    List<Candidate> searchCandidates(String filter);

    AttachmentResource loadAttachmentAsResource(long candidateId, int index);

    Contact convertToContact(Long candidateId);
}