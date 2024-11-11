package com.pcistudio.task.procesor.handler;

public class TaskProcessorPauseException extends Exception {
    public TaskProcessorPauseException(String message) {
        super(message);
    }

    public TaskProcessorPauseException(String message, Throwable cause) {
        super(message, cause);
    }
}
