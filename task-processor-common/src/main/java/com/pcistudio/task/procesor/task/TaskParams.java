package com.pcistudio.task.procesor.task;


import lombok.Builder;
import lombok.Getter;

import java.time.Duration;
import java.time.Instant;


@Getter
@Builder
public class TaskParams {
    private String handlerName;
    private Object payload;
    private Instant executionTime;
    private Duration delay;
}
