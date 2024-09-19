// src/main/java/com/contact/manager/entities/Attachment.java
package com.contact.manager.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.proxy.HibernateProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Objects;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class Attachment {
    ///Yo can not update an attachment

    private static final Logger log = LoggerFactory.getLogger(Attachment.class);
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String fileName;
    private String fileType;
    @NotBlank
    private String filePath; // New field to store file path

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @ManyToOne
    private Contact contact;

    public Long getId() {
        return id;
    }

    public Attachment setId(Long id) {
        this.id = id;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public Attachment setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public String getFileType() {
        return fileType;
    }

    public Attachment setFileType(String fileType) {
        this.fileType = fileType;
        return this;
    }

    public String getFilePath() {
        return filePath;
    }

    public Attachment setFilePath(String filePath) {
        this.filePath = filePath;
        return this;
    }

    public Contact getContact() {
        return contact;
    }

    public Attachment setContact(Contact contact) {
        this.contact = contact;
        return this;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @PostRemove
    public void postRemove() {
        try {
            Path path = Paths.get(filePath);
            Files.deleteIfExists(path);
        } catch (Exception e) {
            log.error("Could not delete file: {}", filePath, e);
        }
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Attachment attachment = (Attachment) o;
        return getId() != null && Objects.equals(getId(), attachment.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }
}