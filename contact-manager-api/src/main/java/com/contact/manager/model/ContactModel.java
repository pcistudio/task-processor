package com.contact.manager.model;

import com.contact.manager.entities.Address;
import com.contact.manager.entities.Contact;
import org.springframework.util.Assert;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

public class ContactModel {
    private Long id;
    private String firstName;
    private String lastName;
    private String jobTitle;
    private String officePhone;
    private String mobile;
    private String email;
    private String description;

    private Address primaryAddress;

    private Address secondaryAddress;

    private List<NoteView> notes;

    private List<AttachmentView> attachments;

    public ContactModel(Contact contact) {
        Assert.notNull(contact, "Contact must not be null");
        this.id = contact.getId();
        this.lastName = contact.getLastName();
        this.firstName = contact.getFirstName();
        this.jobTitle = contact.getJobTitle();
        this.email = contact.getEmail();

        this.officePhone = contact.getOfficePhone();
        this.mobile = contact.getMobile();
        this.description = contact.getDescription();
        this.primaryAddress = contact.getPrimaryAddress();
        this.secondaryAddress = contact.getSecondaryAddress();
        this.notes = NoteView.fromNotes(contact.getNotes());
        this.attachments = AttachmentView.fromAttachments(contact.getAttachments());
    }

    public static ContactModel fromContact(Contact contact) {
        ContactModel contactModel = new ContactModel(contact);
        updateAttachmentUrl(contactModel.getId(), contactModel.getAttachments());
        return contactModel;
    }
    private static void updateAttachmentUrl(Long contactId, List<AttachmentView> attachments) {
        if (attachments != null) {
            attachments
                    .forEach(attachment -> attachment.setUrl(buildUrl(contactId, attachment.getId())));
        }
    }

    private static String buildUrl(Long candidateId, Long attachmentId) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/contacts")
                .path("/" + candidateId.toString())
                .path("/attachments")
                .path("/" + attachmentId.toString())
                .toUriString();
    }

    public Long getId() {
        return id;
    }

    public ContactModel setId(Long id) {
        this.id = id;
        return this;
    }

    public String getLastName() {
        return lastName;
    }

    public ContactModel setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public ContactModel setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public ContactModel setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getOfficePhone() {
        return officePhone;
    }

    public ContactModel setOfficePhone(String officePhone) {
        this.officePhone = officePhone;
        return this;
    }

    public String getMobile() {
        return mobile;
    }

    public ContactModel setMobile(String mobile) {
        this.mobile = mobile;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ContactModel setDescription(String description) {
        this.description = description;
        return this;
    }

    public Address getPrimaryAddress() {
        return primaryAddress;
    }

    public ContactModel setPrimaryAddress(Address primaryAddress) {
        this.primaryAddress = primaryAddress;
        return this;
    }

    public Address getSecondaryAddress() {
        return secondaryAddress;
    }

    public ContactModel setSecondaryAddress(Address secondaryAddress) {
        this.secondaryAddress = secondaryAddress;
        return this;
    }

    public List<NoteView> getNotes() {
        return notes;
    }

    public ContactModel setNotes(List<NoteView> notes) {
        this.notes = notes;
        return this;
    }

    public List<AttachmentView> getAttachments() {
        return attachments;
    }

    public ContactModel setAttachments(List<AttachmentView> attachments) {
        this.attachments = attachments;
        return this;
    }

    public String getFirstName() {
        return firstName;
    }

    public ContactModel setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }
}
