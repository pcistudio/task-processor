package com.pcistudio.task.procesor.handler;

public class TaskProcessorPauseException extends Exception {
    public TaskProcessorPauseException(final String message) {
        super(message);
    }

    public TaskProcessorPauseException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
