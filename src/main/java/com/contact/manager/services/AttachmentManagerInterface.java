package com.contact.manager.services;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public interface AttachmentManagerInterface {
    Path storeAttachment(InputStream inputStream, String originalFileName) throws IOException;
}
