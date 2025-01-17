package com.pcistudio.task.processor.config;

import com.pcistudio.task.procesor.metrics.TaskProcessorMetricsFactory;
import com.pcistudio.task.procesor.metrics.micrometer.DefaultTaskProcessorMetricsFactory;
import com.pcistudio.task.procesor.metrics.micrometer.MicrometerClockAdapter;
import com.pcistudio.task.procesor.metrics.none.NullTaskProcessorMetricsFactory;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.jmx.JmxConfig;
import io.micrometer.jmx.JmxMeterRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.time.Clock;

@Configuration
public class MetricsConfig {
    @Configuration
    @ConditionalOnProperty(prefix = "task.processor.metrics", name = "enable", havingValue = "true")
    static class MicrometerConfig implements EnvironmentAware {
        private Environment environment;

//        @Bean
//        @ConditionalOnMissingBean
//        MeterRegistry meterRegister(Clock clock) {
//            return new JmxMeterRegistry(JmxConfig.DEFAULT, MicrometerClockAdapter.of(clock));
//        }

        @Bean
        @ConditionalOnMissingBean
        TaskProcessorMetricsFactory processingMetrics(ObjectProvider<MeterRegistry> meterRegistryObjectProvider, Tags tags) {
//            meterRegistry.config().commonTags(tags);
            MeterRegistry meterRegistry = meterRegistryObjectProvider.getIfAvailable();
            if (meterRegistry == null)
            {
                meterRegistry = new SimpleMeterRegistry();
            }
            return new DefaultTaskProcessorMetricsFactory(meterRegistry);
        }

        @Bean
        @ConditionalOnMissingBean
        Tags tags() {
            String appName;
            if((appName = environment.getProperty("spring.application.name")) != null) {
                return Tags.of("application", appName);
            }
            return Tags.empty();
        }

        @Override
        public void setEnvironment(Environment environment) {
            this.environment = environment;
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
