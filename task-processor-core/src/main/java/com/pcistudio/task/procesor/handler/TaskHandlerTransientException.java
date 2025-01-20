package com.pcistudio.task.procesor.handler;

/**
 * This exception is meant to be thrown when a transient exception happen in the handler
 *  <code>{@link TaskHandler}</code>
 */
public class TaskHandlerTransientException extends TaskTransientException {
    public TaskHandlerTransientException(final String message) {
        super(message);
    }

    public TaskHandlerTransientException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
