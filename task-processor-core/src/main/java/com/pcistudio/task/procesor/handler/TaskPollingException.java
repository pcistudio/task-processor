package com.pcistudio.task.procesor.handler;

public class TaskPollingException extends RuntimeException {
    public TaskPollingException(String message) {
        super(message);
    }

    public TaskPollingException(String message, Throwable cause) {
        super(message, cause);
    }
}
