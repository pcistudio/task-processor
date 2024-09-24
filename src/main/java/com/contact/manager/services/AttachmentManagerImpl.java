package com.contact.manager.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

public class AttachmentManagerImpl implements AttachmentManager {

    private static final Logger log = LoggerFactory.getLogger(AttachmentManagerImpl.class);
    private final Path attachmentDirectory;
    private final Clock clock;

    public AttachmentManagerImpl(Path attachmentDirectory, Clock clock) {
        this.attachmentDirectory = attachmentDirectory;
        this.clock = clock;
        try {
            log.debug("Creating attachment directory: {}", this.attachmentDirectory);
            Path dir = Files.createDirectories(this.attachmentDirectory);
            log.info("Created attachment directory: {}", dir.toAbsolutePath());
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not create attachment directory", e);
        }
    }

    public AttachmentManagerImpl(String attachmentDirectory, Clock clock) {
        this(Paths.get(attachmentDirectory), clock);
    }

    @Override
    public Path storeAttachment(InputStream inputStream, String originalFileName) throws IOException {
        String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFileName;
        Path directory = attachmentDirectory.resolve(yearMonthDayDirectory());
        Files.createDirectories(directory);
        Path filePath = directory.resolve(uniqueFileName);
        Files.copy(inputStream, filePath);
        log.debug("Stored attachment: {}", filePath);
        return filePath;
    }

    @Override
    public boolean canWrite() {
        return attachmentDirectory
                .toFile()
                .canWrite();
    }

    private Path yearMonthDayDirectory() {
        LocalDate now = LocalDate.now(clock);

        return Paths.get(String.format("%02d", now.getYear()),
                String.format("%02d", now.getMonthValue()),
                String.format("%02d", now.getDayOfMonth())
        );
    }

    @Override
    public Path storeAttachment(MultipartFile file) {
        Path path;
        try (InputStream inputStream = file.getInputStream()) {
            path = storeAttachment(inputStream, file.getOriginalFilename());
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not save attachment", e);
        }
        return path;
    }

    @Override
    public void deleteAttachment(Path fileName) {
        try {
            Files.deleteIfExists(fileName);
            log.info("Deleted attachment: {}", fileName);
        } catch (IOException e) {
            log.error("Could not delete attachment: {}", fileName, e);
        }
    }

    @Override
    public Resource loadAttachmentAsResource(String filePath) {
        try {
            Path file = Paths.get(filePath);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found " + filePath);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File not found " + filePath, ex);
        }
    }
}