package com.pcistudio.task.procesor.handler;


import com.pcistudio.task.procesor.HandlerPropertiesWrapper;
import com.pcistudio.task.procesor.task.TaskInfo;
import com.pcistudio.task.procesor.util.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.nio.channels.ClosedByInterruptException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class TaskProcessor implements Closeable, Runnable {
    private final TaskHandlerProxy taskHandlerProxy;
    private final ThreadPoolExecutor executorService;
    private final HandlerPropertiesWrapper properties;
    private volatile AtomicReference<TaskProcessorState> state = new AtomicReference<>(TaskProcessorState.CREATED);

    TaskProcessor(TaskProcessingContext taskProcessingContext) {
        this.taskHandlerProxy = new TaskHandlerProxy(taskProcessingContext);
        this.properties = taskProcessingContext.getHandlerProperties();
        this.executorService = new ThreadPoolExecutor(Math.max(properties.getMaxParallelTasks() / 2, 1),
                properties.getMaxParallelTasks(),
                1000L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(Math.max(properties.getMaxParallelTasks(), properties.getMaxPoll()) * 2),
                new DefaultThreadFactory("task-handler-" + properties.getHandlerName()));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                close();
            } catch (RuntimeException e) {
                log.error("Error closing task processor", e);
            }
        }));
    }

    public void processTasks() throws InterruptedException, TaskProcessorClosingException {
        log.info("Task processor={} starting", properties.getHandlerName());
        changeCurrentState(TaskProcessorState.RUNNING);

        while (isRunning()) {
            doProcessTasks(taskHandlerProxy.iterator());

            log.debug("Waiting for tasks for processor={}", properties.getHandlerName());
            Thread.sleep(properties.getPollInterval());
        }
    }

    private void changeCurrentState(TaskProcessorState nextState) {
        if (state.get() == TaskProcessorState.CREATED && nextState == TaskProcessorState.RUNNING) {
            state.compareAndSet(TaskProcessorState.CREATED, nextState);
        } else  if (state.get() == TaskProcessorState.CREATED && nextState == TaskProcessorState.SHUTTING_DOWN) {
            state.compareAndSet(TaskProcessorState.CREATED, nextState);
        } else if (state.get() == TaskProcessorState.RUNNING && nextState == TaskProcessorState.SHUTTING_DOWN) {
            state.compareAndSet(TaskProcessorState.RUNNING, nextState);
        } else {
            throw new IllegalStateException("Invalid state transition from " + state.get() + " to " + nextState);
        }
    }

//    public boolean isPaused() {
//        return state.get() == TaskProcessorState.PAUSED;
//    }

    public boolean isShuttingDown() {
        return state.get() == TaskProcessorState.SHUTTING_DOWN;
    }

    public boolean isRunning() {
        return state.get() == TaskProcessorState.RUNNING;
    }

    private void doProcessTasks(Iterator<TaskInfo> tasks) throws InterruptedException, TaskProcessorClosingException {

        while (tasks.hasNext()) {
            TaskInfo task = tasks.next();
            if (isShuttingDown()) {
                log.warn("Task processor={} is shutdown, skipping task={}", properties.getHandlerName(), task.getId());
                throw new TaskProcessorClosingException("Task processor=" + properties.getHandlerName() + " is shutting down");
            }

            try {
                executorService.submit(() -> taskHandlerProxy.process(task));
            } catch (RejectedExecutionException e) {
                log.error("Task processor={} rejected task={}", properties.getHandlerName(), task.getId(), e);
            }
        }
    }


    @Override
    public void close() {
        close(10, TimeUnit.SECONDS);
    }

    private void close(long timeout, TimeUnit unit) {
        changeCurrentState(TaskProcessorState.SHUTTING_DOWN);
        executorService.shutdown();
        try {
            boolean b = executorService.awaitTermination(timeout, unit);
            if (!b) {
                log.warn("Task processor={} not terminated!", properties.getHandlerName());
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.warn("Task processor={} Interrupted!", properties.getHandlerName(), e);
            Thread.currentThread().interrupt();
        }
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

//    public int getMarkForProcessing() {
//        return semaphore.availablePermits();
//    }

    @Override
    public void run() {
        try {
            processTasks();
        } catch (TaskProcessorClosingException e) {
            log.warn("Task processor={} closing", properties.getHandlerName(), e);
        } catch (Exception e) {
            log.error("Error processing task from processor={}", properties.getHandlerName(), e);
        } finally {
            close();
        }
    }

    private enum TaskProcessorState {
        CREATED,
        RUNNING,
        SHUTTING_DOWN
    }

}
