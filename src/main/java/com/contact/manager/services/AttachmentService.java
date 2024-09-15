// src/main/java/com/contact/manager/services/AttachmentService.java
package com.contact.manager.services;

import com.contact.manager.entities.Attachment;
import com.contact.manager.model.AttachmentRequest;
import com.contact.manager.repositories.AttachmentRepository;
import com.contact.manager.repositories.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Path;

@Service
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final ContactRepository contactRepository;
    private final AttachmentManagerInterface attachmentManager;

    @Autowired
    public AttachmentService(AttachmentRepository attachmentRepository, ContactRepository contactRepository, AttachmentManagerInterface attachmentManager) {
        this.attachmentRepository = attachmentRepository;
        this.contactRepository = contactRepository;
        this.attachmentManager = attachmentManager;
    }

    public Attachment updateAttachment(Long attachmentId, AttachmentRequest attachmentRequest) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("Attachment not found"));
        attachment.setFileName(attachmentRequest.getFileName());
        attachment.setFileType(attachmentRequest.getFileType());
        attachment.setFilePath(attachmentRequest.getFilePath());

        return attachmentRepository.save(attachment);
    }

    public void deleteAttachment(Long attachmentId) {
        attachmentRepository.deleteById(attachmentId);
    }

    public Attachment saveAttachment(Long contactId, MultipartFile file) {

        Attachment attachment = new Attachment();

        // Save file path to database
        Path path = getStorePath(file);

        attachment.setFileName(file.getOriginalFilename());
        attachment.setFileType(file.getContentType());
        attachment.setFilePath(path.toString());
        attachment.setContact(contactRepository.getReferenceById(contactId));

        return attachmentRepository.save(attachment);

    }

    private Path getStorePath(MultipartFile file) {
        Path path;
        try (InputStream inputStream = file.getInputStream()) {
            path = attachmentManager.storeAttachment(inputStream, file.getOriginalFilename());
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not save attachment", e);
        }
        return path;
    }
}