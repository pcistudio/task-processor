package com.pcistudio.task.procesor.handler;

public class TaskHandlerException extends Exception {
    public TaskHandlerException(String message) {
        super(message);
    }

    public TaskHandlerException(String message, Throwable cause) {
        super(message, cause);
    }
}
