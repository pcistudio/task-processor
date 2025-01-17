package com.pcistudio.task.procesor.handler;


import java.util.function.Consumer;

public interface CircuitBreakerDecorator {
    void addCircuitOpenListener(CircuitOpenListener circuitOpenListener);

    <T> Consumer<T> decorate(Consumer<T> processTaskInfo);

    void open();
}
