package com.contact.manager.model;

import com.contact.manager.entities.Attachment;
import org.springframework.util.Assert;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


import java.util.Collection;
import java.util.List;

public class AttachmentView {
    private Long id;
    private String fileName;
    private String fileType;
    // Change this value for a fileURL instead of a physical path
//    private String filePath; // New field to store file path
    private String url;
//    private Long contactId;

    public AttachmentView(Attachment attachment) {

        Assert.notNull(attachment, "Attachment must not be null");
        Assert.notNull(attachment.getId(), "Attachment id must not be null");
        this.id = attachment.getId();
        this.fileName = attachment.getFileName();
        this.fileType = attachment.getFileType();
//        this.contactId = attachment.getContact().getId();
//        this.url = buildUrl(attachment.getId());

    }

    public static List<AttachmentView> fromAttachments(Collection<Attachment> attachments) {
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

//    public String getFilePath() {
//        return filePath;
//    }
//
//    public AttachmentView setFilePath(String filePath) {
//        this.filePath = filePath;
//        return this;
//    }


    public String getUrl() {
        return url;
    }

    public AttachmentView setUrl(String url) {
        this.url = url;
        return this;
    }

//    public Long getContactId() {
//        return contactId;
//    }
//
//    public AttachmentView setContactId(Long contactId) {
//        this.contactId = contactId;
//        return this;
//    }
}