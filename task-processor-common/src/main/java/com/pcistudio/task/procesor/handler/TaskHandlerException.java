package com.pcistudio.task.procesor.handler;

/**
 * This exception is meant to be thrown when a non-transient exception happen in the  <code>{@link TaskHandler}</code>
 */
public class TaskHandlerException extends Exception {
    public TaskHandlerException(String message) {
        super(message);
    }

    public TaskHandlerException(String message, Throwable cause) {
        super(message, cause);
    }
}
