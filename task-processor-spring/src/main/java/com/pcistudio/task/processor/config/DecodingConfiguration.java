package com.pcistudio.task.processor.config;

import com.pcistudio.task.procesor.util.decoder.JsonMessageDecoding;
import com.pcistudio.task.procesor.util.decoder.MessageDecoding;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DecodingConfiguration {
    @ConditionalOnMissingBean(MessageDecoding.class)
    MessageDecoding messageEncoding() {
        return new JsonMessageDecoding();
    }
}
