package com.pcistudio.task.procesor.handler;


import java.util.function.Consumer;

public interface CircuitBreakerDecorator {
    <T> Consumer<T> decorate(Consumer<T> processTaskInfo);
    boolean isClosed();
}
