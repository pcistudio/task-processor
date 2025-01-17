package com.pcistudio.task.processor.config;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MeterConfig {
    @Bean
    public MeterFilter customMeterFilter() {
        return new MeterFilter() {
            @Override
            public DistributionStatisticConfig configure(Meter.Id id,
                                                         DistributionStatisticConfig config) {
//                log.info("filtering {}", id.getName());
                if (id.getType() == Meter.Type.TIMER && id.getName().startsWith("task.processor.handler")) {
                    log.trace("Adding percentiles config to {}", id.getName());
                    return DistributionStatisticConfig.builder()
                            .percentiles(0.99, 0.999)
                            .build()
                            .merge(config);
                }
                return config;
            }
        };
    }
}
