package com.contact.manager.model.mapper;

import com.contact.manager.entities.Attachment;
import com.contact.manager.model.AttachmentView;
import org.springframework.util.Assert;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

public class AttachmentIndexMapper extends AttachmentMapper {

    @Override
    public String getAttachmentId(Attachment attachment, int index) {
        return String.valueOf(index);
    }
}
