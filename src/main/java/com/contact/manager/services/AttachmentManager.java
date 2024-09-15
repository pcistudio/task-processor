package com.contact.manager.services;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
public class AttachmentManager implements AttachmentManagerInterface {

    private static final Logger log = LoggerFactory.getLogger(AttachmentManager.class);
    private final Path attachmentDirectory;

    public AttachmentManager(Path attachmentDirectory) {
        this.attachmentDirectory = attachmentDirectory;
    }

    public AttachmentManager(String attachmentDirectory) {
        this.attachmentDirectory = Paths.get(attachmentDirectory);
        try {
            log.debug("Creating attachment directory: {}", this.attachmentDirectory);
            Path dir = Files.createDirectories(this.attachmentDirectory);
            log.info("Created attachment directory: {}", dir.toAbsolutePath());
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not create attachment directory", e);
        }
    }

    @Override
    public Path storeAttachment(InputStream inputStream, String originalFileName) throws IOException {
        String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFileName;
        Path filePath = attachmentDirectory.resolve(uniqueFileName);
        Files.copy(inputStream, filePath);
        return filePath;
    }
}