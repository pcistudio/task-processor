package com.contact.manager.model;

import com.contact.manager.entities.Attachment;
import jakarta.validation.constraints.Size;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.List;

public class AttachmentView {
    private Long id;
    @Size(max = 255)
    private String fileName;
    @Size(max = 50)
    private String fileType;
    @Size(max = 255)
    private String url;

    public AttachmentView(Attachment attachment) {

        Assert.notNull(attachment, "Attachment must not be null");
        Assert.notNull(attachment.getId(), "Attachment id must not be null");
        this.id = attachment.getId();
        this.fileName = attachment.getFileName();
        this.fileType = attachment.getFileType();

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

    public String getUrl() {
        return url;
    }

    public AttachmentView setUrl(String url) {
        this.url = url;
        return this;
    }
}