package com.pcistudio.task.procesor.handler;

public interface TaskHandler<T> {
    void process(T payload);
}
