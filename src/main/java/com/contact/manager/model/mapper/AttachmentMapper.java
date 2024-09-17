package com.contact.manager.model.mapper;

import com.contact.manager.entities.Attachment;
import com.contact.manager.model.AttachmentView;
import org.springframework.util.Assert;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

public abstract class AttachmentMapper implements Mapper<List<Attachment>, List<AttachmentView>> {

    @Override
    public List<AttachmentView> map(List<Attachment> attachments) {
//        Assert.notNull(attachments, "Attachments must not be null");
//        List<AttachmentView> attachmentViews = List.of();
//        for (int i = 0; i < attachments.size(); i++) {
//            Attachment attachment = attachments.get(i);
//            attachmentViews.add(getAttachmentView(attachment, i));
//        }
        return null;
    }

//    private AttachmentView getAttachmentView(Attachment attachment, int index) {
//        AttachmentView attachmentView = new AttachmentView();
//        Assert.notNull(attachment, "Attachment must not be null");
//        attachmentView.setId(attachment.getId());
//        attachmentView.setFileName(attachment.getFileName());
//        attachmentView.setFileType(attachment.getFileType());
//        attachmentView.setUrl(getURL(attachment, index));
//        attachmentView.setContactId(attachment.getContact().getId());
//        return attachmentView;
//    }
//
//    private String getURL(Attachment attachment, int index) {
//        return ServletUriComponentsBuilder.fromCurrentContextPath()
//                .path("/attachments/")
//                .path(getAttachmentId(attachment, index))
//                .toUriString();
//    }

    public abstract String getAttachmentId(Attachment attachment, int index);
}
