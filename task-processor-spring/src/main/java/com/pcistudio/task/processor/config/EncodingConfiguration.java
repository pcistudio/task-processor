package com.pcistudio.task.processor.config;

import com.pcistudio.task.procesor.util.encoder.JsonMessageEncoding;
import com.pcistudio.task.procesor.util.encoder.MessageEncoding;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EncodingConfiguration {
    @ConditionalOnMissingBean(MessageEncoding.class)
    MessageEncoding messageEncoding() {
        return new JsonMessageEncoding();
    }
}
