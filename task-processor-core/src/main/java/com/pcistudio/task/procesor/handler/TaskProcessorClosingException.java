package com.pcistudio.task.procesor.handler;

public class TaskProcessorClosingException extends Exception {
    public TaskProcessorClosingException(String message) {
        super(message);
    }

    public TaskProcessorClosingException(String message, Throwable cause) {
        super(message, cause);
    }
}
