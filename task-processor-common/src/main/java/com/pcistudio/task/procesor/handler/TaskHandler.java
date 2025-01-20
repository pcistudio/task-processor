package com.pcistudio.task.procesor.handler;

import javax.annotation.concurrent.Immutable;

@FunctionalInterface
@Immutable
public interface TaskHandler<T> {
    void process(T payload);
}
