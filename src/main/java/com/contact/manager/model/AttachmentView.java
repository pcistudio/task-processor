package com.contact.manager.model;

import com.contact.manager.entities.Attachment;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Set;

public class AttachmentView {
    private Long id;
    private String fileName;
    private String fileType;
    private String filePath; // New field to store file path
    private Long contactId;

    public AttachmentView(Attachment attachment) {
        Assert.notNull(attachment, "Attachment must not be null");
        this.id = attachment.getId();
        this.fileName = attachment.getFileName();
        this.fileType = attachment.getFileType();
        this.filePath = attachment.getFilePath();
        this.contactId = attachment.getContact().getId();
    }

    public static List<AttachmentView> fromAttachments(Set<Attachment> attachments) {
        if (attachments != null) {
            return attachments.stream().map(AttachmentView::new).toList();
        }
        return List.of();
    }

    public Long getId() {
        return id;
    }

    public AttachmentView setId(Long id) {
        this.id = id;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public AttachmentView setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public String getFileType() {
        return fileType;
    }

    public AttachmentView setFileType(String fileType) {
        this.fileType = fileType;
        return this;
    }

    public String getFilePath() {
        return filePath;
    }

    public AttachmentView setFilePath(String filePath) {
        this.filePath = filePath;
        return this;
    }

    public Long getContactId() {
        return contactId;
    }

    public AttachmentView setContactId(Long contactId) {
        this.contactId = contactId;
        return this;
    }
}