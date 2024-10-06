package com.contact.manager.notification;

import com.contact.manager.entities.SimplePerson;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "notification", indexes = {
        @Index(name = "idx_notification_batch_id", columnList = "batch_id")
})
public class Notification implements SimplePerson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private UUID batchId;
    @Enumerated(EnumType.STRING)
    private PersonType personType;

    @Column(name = "subject", length = 250)
    private String subject;
    @Column(name = "template_name", length = 50)
    private String templateName;
    @Column(name = "message", length = 1024)
    private String message;
    @Column(name = "email", length = 70)
    private String email;
    @Column(name = "first_name", length = 50)
    private String firstName;
    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ProcessStatus status = ProcessStatus.PENDING;
    @Column(name = "last_error_message", length = 1024)
    private String lastErrorMessage;
    @Version
    private Long version;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(updatable = false)
    private Instant updatedAt;

    public Notification() {
    }


    @Override
    public Long getId() {
        return id;
    }

    public Notification setId(Long id) {
        this.id = id;
        return this;
    }

    public PersonType getPersonType() {
        return personType;
    }

    public Notification setPersonType(PersonType personType) {
        this.personType = personType;
        return this;
    }

    public String getSubject() {
        return subject;
    }

    public Notification setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public String getTemplateName() {
        return templateName;
    }

    public Notification setTemplateName(String templateName) {
        this.templateName = templateName;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public Notification setMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    public String getEmail() {
        return email;
    }

    public Notification setEmail(String email) {
        this.email = email;
        return this;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    public Notification setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    public Notification setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Notification setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Notification setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public UUID getBatchId() {
        return batchId;
    }

    public Notification setBatchId(UUID batchId) {
        this.batchId = batchId;
        return this;
    }

    public ProcessStatus getStatus() {
        return status;
    }

    public Notification setStatus(ProcessStatus status) {
        this.status = status;
        return this;
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public Notification setLastErrorMessage(String lastErrorMessage) {
        this.lastErrorMessage = lastErrorMessage;
        return this;
    }
}
