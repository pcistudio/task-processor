package com.contact.manager.model.mapper;

import com.contact.manager.entities.Attachment;
import org.springframework.util.Assert;

public class AttachmentIdMapper extends AttachmentMapper {

    @Override
    public String getAttachmentId(Attachment attachment, int index) {
        Assert.notNull(attachment, "Attachment must not be null");
        Assert.notNull(attachment.getId(), "Attachment id must not be null");
        return attachment.getId().toString();
    }
}
