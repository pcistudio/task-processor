package com.pcistudio.task.procesor.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
public class TaskExecutionTracker implements ExecutionTracker {
    private final Map<Long, TimeFuture> futureMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ThreadPoolExecutor executorService;
    private final Clock clock;
    private final long longTaskTimeMs;
    private final String handlerName;

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public TaskExecutionTracker(String handlerName, final ThreadPoolExecutor executorService, Clock clock,  long longTaskTimeMs, long longTaskCheckIntervalMs, long longTaskCheckInitialDelayMs) {
        this.handlerName = handlerName;
        this.executorService = executorService;
        this.clock = clock;
        this.longTaskTimeMs = longTaskTimeMs;
        scheduler.scheduleAtFixedRate(this::cancelLongWaitingTasks, longTaskCheckIntervalMs, longTaskCheckInitialDelayMs, TimeUnit.MILLISECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                shutdown();
            } catch (RuntimeException e) {
                log.error("Error closing task execution tracker for handlerName={}", handlerName, e);
            }
        }));
    }

    @Override
    public void trackFuture(long taskId, Runnable runnable) {
        if (futureMap.containsKey(taskId)) {
            log.warn("There is an execution with that taskId={} for handlerName={}", taskId, handlerName);
            return;
        }

        CompletableFuture<?> future = CompletableFuture.runAsync(runnable, executorService);

        futureMap.put(taskId, new TimeFuture(clock.millis(), future));
        future.whenComplete((result, throwable) -> {
            futureMap.remove(taskId);
            if (log.isTraceEnabled()) {
                log.trace("Task={} is completed in handlerName={}", taskId, handlerName);
            }
            if (throwable != null) {
                log.error("Error running task={} in handlerName={}", taskId, handlerName, throwable);
            }
        });
    }

    @Override
    public long getTaskCount() {
        return futureMap.size();
    }

    @Override
    public long getLongWaitingTaskCount() {
        return getLongWaitingTasks().size();
    }

    public int getActiveProcessingCount() {
        return executorService.getActiveCount();
    }

    public int getActiveWaitingInMemory() {
        return executorService.getQueue().size();
    }


    @Override
    public void shutdown() {
        executorService.shutdown();
        scheduler.shutdown();
        waitTermination(executorService);
        waitTermination(scheduler);
    }

    private void cancelLongWaitingTasks() {
        getLongWaitingTasks().forEach(entry -> {
            long taskId = entry.getKey();
            TimeFuture timeFuture = entry.getValue();

            log.warn("Task={} has been running for more than {} millis in handlerName={}. runningTime={}", taskId, longTaskTimeMs, handlerName, clock.millis() - timeFuture.timestamp());

            timeFuture.future().cancel(true);
            if (timeFuture.future().isDone()) {
                log.warn("Task={} is cancelled after long run in handlerName={}", taskId, handlerName);
                futureMap.remove(taskId);
            } else {
                log.warn("Long running task={} cannot be cancelled in handlerName={}", taskId, handlerName);
            }
        });
    }

    private List<Map.Entry<Long, TimeFuture>> getLongWaitingTasks() {
        return futureMap.entrySet().stream()
                .filter(entry -> clock.millis() - entry.getValue().timestamp() > longTaskTimeMs)
                .toList();
    }

    private void waitTermination(ExecutorService executor) {
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private record TimeFuture(long timestamp, CompletableFuture<?> future) {
    }
}