package com.contact.manager.model;

import com.contact.manager.entities.Attachment;
import org.springframework.core.io.Resource;

public class AttachmentResource {
    private Resource resource;
    private Attachment attachment;

    public Resource getResource() {
        return resource;
    }

    public AttachmentResource setResource(Resource resource) {
        this.resource = resource;
        return this;
    }

    public Attachment getAttachment() {
        return attachment;
    }

    public AttachmentResource setAttachment(Attachment attachment) {
        this.attachment = attachment;
        return this;
    }
}
