// src/main/java/com/contact/manager/entities/Candidate.java
package com.contact.manager.entities;

import com.contact.manager.entities.contraints.AtLeastOneField;
import com.contact.manager.entities.converter.JsonConverters;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.*;

@Entity
@EntityListeners(AuditingEntityListener.class)
@AtLeastOneField(fields = {"officePhone", "mobile", "email"}, message = "At least one of the fields=[officePhone, mobile, email] must be present")
public class Candidate implements Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    private String firstName;
    private String lastName;
    private String jobTitle;
    private String officePhone;
    private String mobile;
    private String email;
    private String description;
    private boolean markForInterview;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Convert(converter = JsonConverters.AddressJsonConverter.class)
    @Column(columnDefinition = "TEXT")
    @Valid
    private Address primaryAddress;

    @Convert(converter = JsonConverters.AddressJsonConverter.class)
    @Column(columnDefinition = "TEXT")
    @Valid
    private Address secondaryAddress;

    @Convert(converter = JsonConverters.NoteListJsonConverter.class)
    @Column(columnDefinition = "TEXT")
    @Valid
    private List<Note> notes = new ArrayList<>();

    @Convert(converter = JsonConverters.AttachmentListJsonConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<Attachment> attachments = new ArrayList<>();

    @ManyToOne(optional = false)
    private Position position;

    public Position getPosition() {
        return position;
    }

    public Candidate setPosition(Position position) {
        this.position = position;
        return this;
    }

    public Long getId() {
        return id;
    }

    public Candidate setId(Long id) {
        this.id = id;
        return this;
    }

    public String getFirstName() {
        return firstName;
    }

    public Candidate setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public String getLastName() {
        return lastName;
    }

    public Candidate setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public Candidate setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
        return this;
    }

    public String getOfficePhone() {
        return officePhone;
    }

    public Candidate setOfficePhone(String officePhone) {
        this.officePhone = officePhone;
        return this;
    }

    public String getMobile() {
        return mobile;
    }

    public Candidate setMobile(String mobile) {
        this.mobile = mobile;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public Candidate setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Candidate setDescription(String description) {
        this.description = description;
        return this;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Candidate setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public Candidate setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public Address getPrimaryAddress() {
        return primaryAddress;
    }

    public Candidate setPrimaryAddress(Address primaryAddress) {
        this.primaryAddress = primaryAddress;
        return this;
    }

    public Address getSecondaryAddress() {
        return secondaryAddress;
    }

    public Candidate setSecondaryAddress(Address secondaryAddress) {
        this.secondaryAddress = secondaryAddress;
        return this;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public Candidate setNotes(List<Note> notes) {
        this.notes = notes;
        return this;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public Candidate setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
        return this;
    }

    public boolean isMarkForInterview() {
        return markForInterview;
    }

    public Candidate setMarkForInterview(boolean markForInterview) {
        this.markForInterview = markForInterview;
        return this;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Candidate candidate = (Candidate) o;
        return getId() != null && Objects.equals(getId(), candidate.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }
}