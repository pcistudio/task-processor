package com.contact.manager.config;

import com.contact.manager.services.AttachmentManagerImpl;
import com.contact.manager.services.AttachmentManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class CommonConfig {

    @Value("${attachment.directory}")
    private String attachmentDirectory;

    @Bean
    public AttachmentManager attachmentManager() {
        return new AttachmentManagerImpl(attachmentDirectory, Clock.systemDefaultZone());
    }
}
