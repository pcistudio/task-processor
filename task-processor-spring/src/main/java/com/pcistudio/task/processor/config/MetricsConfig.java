package com.pcistudio.task.processor.config;

import com.pcistudio.task.procesor.metrics.TaskProcessorMetricsFactory;
import com.pcistudio.task.procesor.metrics.micrometer.DefaultTaskProcessorMetricsFactory;
import com.pcistudio.task.procesor.metrics.none.NullTaskProcessorMetricsFactory;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class MetricsConfig {

    @Configuration
    @ConditionalOnProperty(prefix = "task.processor.metrics", name = "enable", havingValue = "true")
    @RequiredArgsConstructor
    static class MicrometerConfig {
        private final Environment environment;

        @Bean
        @ConditionalOnMissingBean
        TaskProcessorMetricsFactory processingMetrics(ObjectProvider<MeterRegistry> meterRegistryObjectProvider) {
            MeterRegistry meterRegistry = meterRegistryObjectProvider.getIfAvailable();
            if (meterRegistry == null) {
                meterRegistry = new SimpleMeterRegistry();
            }
            return new DefaultTaskProcessorMetricsFactory(meterRegistry);
        }

        @Bean
        @ConditionalOnMissingBean
        Tags tags() {
            String appName = environment.getProperty("spring.application.name");
            if (appName != null) {
                return Tags.of("application", appName);
            }
            return Tags.empty();
        }
    }

    @Configuration
    @ConditionalOnProperty(prefix = "task.processor.metrics", name = "enable", havingValue = "false", matchIfMissing = true)
    static class IgnoreMetricConfig {
        @Bean
        @ConditionalOnMissingBean
        TaskProcessorMetricsFactory processingMetrics() {
            return new NullTaskProcessorMetricsFactory();
        }
    }
}
