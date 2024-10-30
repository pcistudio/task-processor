package com.pcistudio.task.procesor;

import com.pcistudio.task.procesor.handler.TaskHandler;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.util.Set;

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
//
//    public String getHandlerClass() {
//        return delegate.getHandlerClass();
//    }
//
//    public String getHandlerMethod() {
//        return delegate.getHandlerMethod();
//    }

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

    public long getPollInterval() {
        return delegate.getPollInterval();
    }

    public long getRequeueIntervalMs() {
        return delegate.getRequeueInterval();
    }


    public String getTableName() {
        return "task_info_" + delegate.getTableName().trim();
    }

    public Set<Class<? extends RuntimeException>> getTransientExceptions() {
        return delegate.getTransientExceptions();
//        return delegate.getTransientExceptions()
//                .stream()
//                .map(exName -> {
//                    try {
//                        return (RuntimeException) Class.forName(exName)
//                                .getConstructor()
//                                .newInstance();
//                    } catch (NoSuchMethodException | ClassNotFoundException | InstantiationException |
//                             IllegalAccessException | InvocationTargetException e) {
//                        throw new RuntimeException(e);
//                    }
//                })
//                .collect(Collectors.toSet());
    }

    public Duration getProcessingExpire() {
        return delegate.getProcessingExpire();
    }

    public Duration getProcessingGracePeriod() {
        return delegate.getProcessingGracePeriod();
    }

    public TaskHandler getTaskHandler() {
        return delegate.getTaskHandler();
    }
//    private Class<?> getPayloadType() {
//        try {
//            return Class.forName(delegate.getPayloadType());
//        } catch (ClassNotFoundException e) {
//            throw new RuntimeException(e);
//        }
//    }
}
