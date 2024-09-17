// src/main/java/com/contact/manager/services/AttachmentService.java
package com.contact.manager.services;

import com.contact.manager.entities.Attachment;
import com.contact.manager.model.AttachmentResource;
import com.contact.manager.repositories.AttachmentRepository;
import com.contact.manager.repositories.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Path;

@Service
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final ContactRepository contactRepository;
    private final AttachmentManager attachmentManager;

    @Autowired
    public AttachmentService(AttachmentRepository attachmentRepository, ContactRepository contactRepository, AttachmentManager attachmentManager) {
        this.attachmentRepository = attachmentRepository;
        this.contactRepository = contactRepository;
        this.attachmentManager = attachmentManager;
    }

    public void deleteAttachment(Long attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("Attachment not found"));

        attachmentRepository.delete(attachment);

    }

    public Attachment saveAttachment(Long contactId, MultipartFile file) {

        Attachment attachment = new Attachment();

        // Save file path to database
        Path path = getStorePath(file);

        attachment.setFileName(file.getOriginalFilename());
        attachment.setFileType(file.getContentType());
        attachment.setFilePath(path.toAbsolutePath().toString());
        attachment.setContact(contactRepository.getReferenceById(contactId));

        return attachmentRepository.save(attachment);

    }
// TODO note should not be modified immutable
    private Path getStorePath(MultipartFile file) {
        Path path;
        try (InputStream inputStream = file.getInputStream()) {
            path = attachmentManager.storeAttachment(inputStream, file.getOriginalFilename());
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not save attachment", e);
        }
        return path;
    }

    public AttachmentResource loadAttachmentAsResource(long contactId, long attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("Attachment not found"));
        Assert.isTrue(attachment.getContact().getId() == contactId, "Contact does not match for attachment=" + attachmentId + " and contact=" + contactId);
        Resource resource = attachmentManager.loadAttachmentAsResource(attachment.getFilePath());

        return new AttachmentResource()
                .setAttachment(attachment)
                .setResource(resource);
    }
}