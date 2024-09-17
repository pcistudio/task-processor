package com.contact.manager.services;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public interface AttachmentManager {
    Path storeAttachment(InputStream inputStream, String originalFileName) throws IOException;

    Path storeAttachment(MultipartFile file);

    void deleteAttachment(Path fileName);

    Resource loadAttachmentAsResource(String filePath);
}
