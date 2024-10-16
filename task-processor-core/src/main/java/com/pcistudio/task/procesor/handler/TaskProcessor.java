package com.pcistudio.task.procesor.handler;


import com.pcistudio.task.procesor.HandlerPropertiesWrapper;
import com.pcistudio.task.procesor.task.TaskInfo;
import com.pcistudio.task.procesor.util.DefaultThreadFactory;
import com.pcistudio.task.procesor.util.decoder.MessageDecoding;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
public class TaskProcessor implements Closeable {
    private final TaskInfoService taskInfoService;
    private final HandlerPropertiesWrapper handlerPropertiesWrapper;
    private final TaskHandlerProxy taskHandlerProxy;
    private final ThreadPoolExecutor executorService;
    private final Semaphore semaphore;
//    private final MessageDecoding messageDecoding;

    public TaskProcessor(TaskInfoService taskInfoService, HandlerPropertiesWrapper handlerPropertiesWrapper, TaskHandler taskHandlerProxy, MessageDecoding messageDecoding) {
        this.taskInfoService = taskInfoService;
        this.handlerPropertiesWrapper = handlerPropertiesWrapper;
        this.taskHandlerProxy = new TaskHandlerProxy(
                TaskProcessingContext.builder()
                        .handlerProperties(handlerPropertiesWrapper)
                        .taskHandler(taskHandlerProxy)
                        .taskInfoService(taskInfoService)
                        .transientExceptions(handlerPropertiesWrapper.getTransientExceptions())
                        //TODO This should go in the parameters
                        .retryManager(new FixRetryManager(handlerPropertiesWrapper.getRetryDelayMs(), handlerPropertiesWrapper.getMaxRetries()))
                        // TODO This probably is not use here messageDecoding
                        .messageDecoding(messageDecoding)
                        .build()
        );
        this.executorService = new ThreadPoolExecutor(Math.max(handlerPropertiesWrapper.getMaxParallelTasks() / 2, 1),
                handlerPropertiesWrapper.getMaxParallelTasks(),
                1000L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                new DefaultThreadFactory("task-processor-" + handlerPropertiesWrapper.getHandlerName()));

        semaphore = new Semaphore(handlerPropertiesWrapper.getMaxParallelTasks() * 2);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                close();
            } catch (RuntimeException e) {
                log.error("Error closing task processor", e);
            }
        }));
    }

//ADD metrics
    public void processTasks() throws InterruptedException {
        List<TaskInfo> tasks = taskInfoService.poll(handlerPropertiesWrapper.getHandlerName() ,handlerPropertiesWrapper.getMaxPoll());
        while (!tasks.isEmpty()) {
            doProcessTasks(tasks);
            tasks = taskInfoService.poll(handlerPropertiesWrapper.getHandlerName(), handlerPropertiesWrapper.getMaxPoll());
        }
    }

    private void doProcessTasks(List<TaskInfo> tasks) throws InterruptedException {
        for (TaskInfo task : tasks) {
            try {
                semaphore.acquire();
                Future<?> taskFuture = executorService.submit(() -> taskHandlerProxy.process(task));
                taskFuture.get();
            }  catch (InterruptedException e) {
                log.error("Interrupted processing taskId={}", task.getId(), e);
                throw e;
            }  catch (ExecutionException e) {
                log.error("Error processing taskId={}", task.getId(),  e);
            } finally {
                semaphore.release();
            }
        }
    }

    @Override
    public void close() {
        executorService.shutdown();
        try {
            boolean b = executorService.awaitTermination(10, TimeUnit.SECONDS);
            if (!b) {
                log.warn("Task processor={} not terminated!", handlerPropertiesWrapper.getHandlerName());
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.warn("Task processor={} Interrupted!", handlerPropertiesWrapper.getHandlerName(), e);
            Thread.currentThread().interrupt();
        }

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
}
