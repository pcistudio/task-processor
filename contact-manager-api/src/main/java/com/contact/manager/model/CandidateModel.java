package com.contact.manager.model;

import com.contact.manager.entities.Address;
import com.contact.manager.entities.Candidate;
import jakarta.validation.constraints.Size;
import org.springframework.util.Assert;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

public class CandidateModel {
    private Long id;
    @Size(max = 70)
    private String firstName;
    @Size(max = 70)
    private String lastName;
    @Size(max = 255)
    private String jobTitle;
    @Size(max = 20)
    private String officePhone;
    @Size(max = 20)
    private String mobile;
    @Size(max = 60)
    private String email;
    @Size(max = 255)
    private String description;
    private boolean markForInterview;

    private Address primaryAddress;

    private Address secondaryAddress;

    private List<NoteView> notes;

    private List<AttachmentView> attachments;

    private PositionView position;

    private CandidateModel(Candidate candidate) {
        Assert.notNull(candidate, "Candidate must not be null");
        this.id = candidate.getId();
        this.lastName = candidate.getLastName();
        this.firstName = candidate.getFirstName();
        this.markForInterview = candidate.isMarkForInterview();
        this.jobTitle = candidate.getJobTitle();
        this.email = candidate.getEmail();

        this.officePhone = candidate.getOfficePhone();
        this.mobile = candidate.getMobile();
        this.description = candidate.getDescription();
        this.primaryAddress = candidate.getPrimaryAddress();
        this.secondaryAddress = candidate.getSecondaryAddress();
        this.notes = NoteView.fromNotes(candidate.getNotes());
        this.attachments = AttachmentView.fromAttachments(candidate.getAttachments());
        this.position = PositionView.from(candidate.getPosition());
    }

    public static CandidateModel fromCandidate(Candidate candidate) {
        CandidateModel candidateModel = new CandidateModel(candidate);
        updateAttachmentUrl(candidateModel.getId(), candidateModel.getAttachments());
        return candidateModel;
    }

    private static void updateAttachmentUrl(Long candidateId, List<AttachmentView> attachments) {
        if (attachments != null) {
            attachments.forEach(attachment -> {
                attachment.setUrl(buildUrl(candidateId, attachment.getId()));
            });
        }
    }

    private static String buildUrl(Long candidateId, Long attachmentId) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/candidates")
                .path("/" + candidateId.toString())
                .path("/attachments")
                .path("/" + attachmentId.toString())
                .toUriString();
    }

    public Long getId() {
        return id;
    }

    public CandidateModel setId(Long id) {
        this.id = id;
        return this;
    }

    public String getLastName() {
        return lastName;
    }

    public CandidateModel setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public CandidateModel setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public CandidateModel setEmail(String email) {
        this.email = email;
        return this;
    }

    public boolean isMarkForInterview() {
        return markForInterview;
    }

    public CandidateModel setMarkForInterview(boolean markForInterview) {
        this.markForInterview = markForInterview;
        return this;
    }

    public String getOfficePhone() {
        return officePhone;
    }

    public CandidateModel setOfficePhone(String officePhone) {
        this.officePhone = officePhone;
        return this;
    }

    public String getMobile() {
        return mobile;
    }

    public CandidateModel setMobile(String mobile) {
        this.mobile = mobile;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public CandidateModel setDescription(String description) {
        this.description = description;
        return this;
    }

    public Address getPrimaryAddress() {
        return primaryAddress;
    }

    public CandidateModel setPrimaryAddress(Address primaryAddress) {
        this.primaryAddress = primaryAddress;
        return this;
    }

    public Address getSecondaryAddress() {
        return secondaryAddress;
    }

    public CandidateModel setSecondaryAddress(Address secondaryAddress) {
        this.secondaryAddress = secondaryAddress;
        return this;
    }

    public List<NoteView> getNotes() {
        return notes;
    }

    public CandidateModel setNotes(List<NoteView> notes) {
        this.notes = notes;
        return this;
    }

    public List<AttachmentView> getAttachments() {
        return attachments;
    }

    public CandidateModel setAttachments(List<AttachmentView> attachments) {
        this.attachments = attachments;
        return this;
    }

    public String getFirstName() {
        return firstName;
    }

    public CandidateModel setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public PositionView getPosition() {
        return position;
    }

    public CandidateModel setPosition(PositionView position) {
        this.position = position;
        return this;
    }
}
