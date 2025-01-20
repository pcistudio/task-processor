package com.pcistudio.task.procesor.handler;

public class TaskPollingException extends RuntimeException {
    public TaskPollingException(final String message) {
        super(message);
    }

    public TaskPollingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
