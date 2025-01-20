package com.pcistudio.task.procesor.metrics;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ProcessorStats {
    private final long trackedTaskCount;
    private final long trackedLongWaitingTaskCount;
    private final int circuitBreakerClosed;
}
