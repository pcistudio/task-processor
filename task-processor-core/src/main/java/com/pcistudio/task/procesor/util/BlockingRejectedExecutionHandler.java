package com.pcistudio.task.procesor.util;

import lombok.NoArgsConstructor;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

@NoArgsConstructor
public class BlockingRejectedExecutionHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(final Runnable runnable, final ThreadPoolExecutor executor) {
            try {
                executor.getQueue().put(runnable);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RejectedExecutionException("Task " + runnable+ " rejected due to interruption", e);
            }
        }
    }