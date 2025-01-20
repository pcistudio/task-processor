package com.pcistudio.task.procesor.handler;

/**
 * This exception is meant to be thrown when a transient exception happen in the library
 * for a transient exception in the handler better use <code>{@link TaskHandlerTransientException}</code>
 */
public class TaskTransientException extends RuntimeException {
    public TaskTransientException(final String message) {
        super(message);
    }

    public TaskTransientException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
