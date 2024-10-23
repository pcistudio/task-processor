package com.pcistudio.task.procesor.handler;


import com.pcistudio.task.procesor.HandlerPropertiesWrapper;
import com.pcistudio.task.procesor.task.TaskInfo;
import com.pcistudio.task.procesor.util.DefaultThreadFactory;
import com.pcistudio.task.procesor.util.decoder.MessageDecoding;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class TaskProcessor implements Closeable, Runnable {
    private final TaskHandlerProxy taskHandlerProxy;
    private final ThreadPoolExecutor executorService;
    private final HandlerPropertiesWrapper properties;
    private final Semaphore semaphore;
    private volatile AtomicReference<TaskProcessorState> state = new AtomicReference<>(TaskProcessorState.CREATED);

    TaskProcessor(TaskProcessingContext taskProcessingContext) {
        this.taskHandlerProxy = new TaskHandlerProxy(taskProcessingContext);
        this.properties = taskProcessingContext.getHandlerProperties();
        this.executorService = new ThreadPoolExecutor(Math.max(properties.getMaxParallelTasks() / 2, 1),
                properties.getMaxParallelTasks(),
                1000L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                new DefaultThreadFactory("task-processor-" + properties.getHandlerName()));

        this.semaphore = new Semaphore(properties.getMaxParallelTasks() * 2);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                close();
            } catch (RuntimeException e) {
                log.error("Error closing task processor", e);
            }
        }));
    }

    public void processTasks() throws InterruptedException, TaskProcessorClosingException {
        changeCurrentState(TaskProcessorState.RUNNING);
        List<TaskInfo> tasks;
        while (isRunning() && !(tasks = taskHandlerProxy.poll()).isEmpty()) {
            doProcessTasks(tasks);
        }
    }

    private void changeCurrentState(TaskProcessorState nextState) {
        if (state.get() == TaskProcessorState.CREATED && nextState == TaskProcessorState.RUNNING) {
            state.compareAndSet(TaskProcessorState.CREATED, nextState);
        } else if (state.get() == TaskProcessorState.PAUSED && nextState == TaskProcessorState.RUNNING) {
            state.compareAndSet(TaskProcessorState.PAUSED, nextState);
        } else if (state.get() == TaskProcessorState.PAUSED && nextState == TaskProcessorState.SHUTTING_DOWN) {
            state.compareAndSet(TaskProcessorState.PAUSED, nextState);
        } else if (state.get() == TaskProcessorState.RUNNING && nextState == TaskProcessorState.PAUSED) {
            state.compareAndSet(TaskProcessorState.RUNNING, nextState);
        } else if (state.get() == TaskProcessorState.RUNNING && nextState == TaskProcessorState.SHUTTING_DOWN) {
            state.compareAndSet(TaskProcessorState.RUNNING, nextState);
        } else {
            throw new IllegalStateException("Invalid state transition from " + state.get() + " to " + nextState);
        }
    }

    public boolean isPaused() {
        return state.get() == TaskProcessorState.PAUSED;
    }

    public boolean isShuttingDown() {
        return state.get() == TaskProcessorState.SHUTTING_DOWN;
    }

    public boolean isRunning() {
        return state.get() == TaskProcessorState.RUNNING;
    }

    private void doProcessTasks(List<TaskInfo> tasks) throws InterruptedException, TaskProcessorClosingException {
        for (TaskInfo task : tasks) {
            if (isShuttingDown()) {
                log.warn("Task processor={} is shutdown, skipping task={}", properties.getHandlerName(), task.getId());
                throw new TaskProcessorClosingException("Task processor=" + properties.getHandlerName() + " is shutting down");
            }
            try {
                semaphore.acquire();
                executorService.submit(() -> taskHandlerProxy.process(task));
            } catch (InterruptedException e) {
                log.error("Interrupted processing taskId={}", task.getId(), e);
                throw e;
            } finally {
                semaphore.release();
            }
        }
    }


    @Override
    public void close() {
        changeCurrentState(TaskProcessorState.SHUTTING_DOWN);
        executorService.shutdown();
        try {
            boolean b = executorService.awaitTermination(10, TimeUnit.SECONDS);
            if (!b) {
                log.warn("Task processor={} not terminated!", properties.getHandlerName());
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.warn("Task processor={} Interrupted!", properties.getHandlerName(), e);
            Thread.currentThread().interrupt();
        }
    }

    public void pause() {
        changeCurrentState(TaskProcessorState.PAUSED);
    }

    public String getHandlerName() {
        return properties.getHandlerName();
    }

    public int getActiveProcessingCount() {
        return executorService.getActiveCount();
    }

    public int getActiveWaitingInMemory() {
        return executorService.getQueue().size();
    }

    public int getMarkForProcessing() {
        return semaphore.availablePermits();
    }

    @Override
    public void run() {
        try {
            processTasks();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (TaskProcessorClosingException e) {
            throw new RuntimeException(e);
        }
    }

    private enum TaskProcessorState {
        CREATED,
        RUNNING,
        SHUTTING_DOWN,
        PAUSED
    }

}
