package com.pcistudio.task.procesor.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultThreadFactory implements ThreadFactory {
    private final AtomicInteger threadCounter = new AtomicInteger();
    private String namePrefix;
    public DefaultThreadFactory(final String namePrefix) {
        this.namePrefix = namePrefix;
    }

    @Override
    public Thread newThread(final Runnable runnable) {

        final Thread thread = new Thread(runnable,
                namePrefix + "-" + threadCounter.getAndIncrement());
        if (thread.isDaemon()) {
            thread.setDaemon(false);
        }
        if (thread.getPriority() != Thread.NORM_PRIORITY) {
            thread.setPriority(Thread.NORM_PRIORITY);
        }
        return thread;
    }
}