package com.pcistudio.task.procesor;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
public class HandlerPropertiesWrapper {
    private HandlerProperties delegate;

    public HandlerPropertiesWrapper(HandlerProperties delegate) {
        this.delegate = delegate;
    }

    public String getHandlerName() {
        return delegate.getHandlerName();
    }

    public String getHandlerClass() {
        return delegate.getHandlerClass();
    }

    public String getHandlerMethod() {
        return delegate.getHandlerMethod();
    }

    public int getMaxRetries() {
        return delegate.getMaxRetries();
    }

    public int getRetryDelayMs() {
        return delegate.getRetryDelayMs();
    }

    public int getMaxParallelTasks() {
        return delegate.getMaxParallelTasks();
    }

    public boolean isExponentialBackoff() {
        return delegate.isExponentialBackoff();
    }

    public int getMaxPoll() {
        return delegate.getMaxPoll();
    }

    public String getTableName() {
        return "task_info_" + delegate.getTableName().trim();
    }

    public Set<RuntimeException> getTransientExceptions() {

        return delegate.getTransientExceptions()
                .stream()
                .map(exName -> {
                    try {
                        return (RuntimeException) Class.forName(exName)
                                .getConstructor()
                                .newInstance();
                    } catch (NoSuchMethodException | ClassNotFoundException | InstantiationException |
                             IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toSet());
    }

//    private Class<?> getPayloadType() {
//        try {
//            return Class.forName(delegate.getPayloadType());
//        } catch (ClassNotFoundException e) {
//            throw new RuntimeException(e);
//        }
//    }
}
