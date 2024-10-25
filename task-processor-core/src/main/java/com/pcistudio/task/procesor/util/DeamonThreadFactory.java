package com.pcistudio.task.procesor.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class DeamonThreadFactory implements ThreadFactory {
    private final AtomicInteger threadCounter = new AtomicInteger();
    private String namePrefix;
    public DeamonThreadFactory(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    public Thread newThread(Runnable runnable) {

        Thread thread = new Thread(runnable,
                namePrefix + "-" + threadCounter.getAndIncrement());
        if (!thread.isDaemon())
            thread.setDaemon(true);
        if (thread.getPriority() != Thread.NORM_PRIORITY)
            thread.setPriority(Thread.NORM_PRIORITY);
        return thread;
    }
}