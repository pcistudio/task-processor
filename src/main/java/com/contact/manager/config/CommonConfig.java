package com.contact.manager.config;

import com.contact.manager.services.AttachmentManager;
import com.contact.manager.services.AttachmentManagerInterface;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonConfig {

    @Value("${attachment.directory}")
    private String attachmentDirectory;

    @Bean
    public AttachmentManagerInterface attachmentManager() {
        return new AttachmentManager(attachmentDirectory);
    }
}
