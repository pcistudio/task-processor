package com.pcistudio.task.procesor.handler;

public class TaskProcessorClosingException extends Exception {
    public TaskProcessorClosingException(final String message) {
        super(message);
    }

    public TaskProcessorClosingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
