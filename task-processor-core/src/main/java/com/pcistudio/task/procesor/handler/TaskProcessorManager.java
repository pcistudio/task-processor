package com.pcistudio.task.procesor.handler;


import com.pcistudio.task.procesor.metrics.*;
import com.pcistudio.task.procesor.util.DeamonThreadFactory;
import com.pcistudio.task.procesor.util.DefaultThreadFactory;
import com.pcistudio.task.procesor.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
public class TaskProcessorManager implements TaskProcessorLifecycleManager {
    private final Map<String, TaskHolder> processorMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService requeueExecutor = Executors.newScheduledThreadPool(1, new DefaultThreadFactory("task-requeue"));
    //TODO for now keep the metrics in here but this will need to be created from a factory
    //
    private final TaskProcessorMetricsFactory taskProcessorMetricsFactory;
    private final ManagerStats managerStats = new ManagerStats();
    private final TaskProcessorManagerMetrics managerMetrics;

    public TaskProcessorManager(TaskProcessorMetricsFactory taskProcessorMetricsFactory) {
        this.taskProcessorMetricsFactory = taskProcessorMetricsFactory;
        this.managerMetrics = this.taskProcessorMetricsFactory.createManagerMetrics(this::managerStats);
    }

    private ManagerStats managerStats() {
        return managerStats
                .setHandlersRegisteredCount(processorMap.size())
                .setHandlersRunningCount(
                        processorMap.values().stream()
                                .filter(TaskHolder::isRunning)
                                .count()
                )
                .setHandlersPausedCount(
                        processorMap.values().stream()
                                .filter(TaskHolder::isPaused)
                                .count()
                );
    }

    public void createTaskProcessor(TaskProcessingContext taskProcessingContext) {
        String handlerName = taskProcessingContext.getHandlerProperties().getHandlerName();
        if (this.processorMap.containsKey(handlerName)) {
            throw new IllegalStateException("Task processor with name " + handlerName + " already exists");
        }
        this.processorMap.put(handlerName, new TaskHolder(taskProcessingContext, taskProcessorMetricsFactory));
        long requeueIntervalMs = taskProcessingContext.getHandlerProperties().getRequeueIntervalMs();
        this.requeueExecutor.scheduleWithFixedDelay(this::requeueTimeoutTask, requeueIntervalMs, requeueIntervalMs, TimeUnit.MILLISECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread(this::closeRequestExecutor));

        if(taskProcessingContext.getHandlerProperties().isAutoStartEnabled()) {
            log.info("Auto starting handlerName={}", handlerName);
            start(handlerName);
        }
    }

    private void closeRequestExecutor() {
        try {
            requeueExecutor.shutdown();
            boolean awaitTermination = requeueExecutor.awaitTermination(10, TimeUnit.SECONDS);
            if (!awaitTermination) {
                requeueExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Error closing task processor", e);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void close() {
        Iterator<Map.Entry<String, TaskHolder>> iterator = processorMap.entrySet().iterator();
        TaskHolder taskHolder;
        while (iterator.hasNext()) {
            taskHolder = iterator.next().getValue();
            taskHolder.close();
        }
        closeRequestExecutor();
    }

    @Override
    public void start() {
        for (String handler : processorMap.keySet()) {
            start(handler);
        }
    }

    @Override
    public void close(String handlerName) {
        processorMap.get(handlerName).close();
    }


    @Override
    public void start(String handlerName) {
        processorMap.get(handlerName).start();
    }

    public Map<String, Integer> stats(String handlerName) {
        return processorMap.get(handlerName).stats();
    }

    @Override
    public void restart(String handlerName) {
        processorMap.get(handlerName).restart();
    }

    public TaskProcessor.EventPublisher getEventPublisher(String handlerName) {
        return processorMap.get(handlerName).getEventPublisher();
    }

    // TODO fix the for and use the same idea than start
    // Do a listener or observer to be able to execute code when this is trigger
    public void requeueTimeoutTask() {
        for (Map.Entry<String, TaskHolder> handlerNameEntry : processorMap.entrySet()) {
            String handlerName = handlerNameEntry.getKey();
            TaskHolder taskHolder = handlerNameEntry.getValue();
            try {
                if (taskHolder.taskProcessor.notStarted()) {
                    log.trace("Task processor not started, skipping requeue for handlerName={}", handlerName);
                    continue;
                }
                taskHolder.requeueTimeoutTask();
            } catch (RuntimeException e) {
                log.error("Error requeue handlerName={}", handlerName, e);
            }
        }
    }

    private static class TaskHolder {
        private static final ThreadFactory THREAD_FACTORY = new DeamonThreadFactory("task-processor");
        private final TaskProcessingContext taskProcessingContext;
        private final TaskProcessorMetricsFactory taskProcessorMetricsFactory;
        private TaskProcessor taskProcessor;
        private Thread thread;
        private Exception lastException = null;
        private TaskProcessorMetrics taskProcessorMetrics;


        public TaskHolder(TaskProcessingContext taskProcessingContext, TaskProcessorMetricsFactory taskProcessorMetricsFactory) {
            this.taskProcessingContext = taskProcessingContext;
            this.taskProcessorMetrics = taskProcessorMetricsFactory.createHandlerMetrics(taskProcessingContext.getHandlerProperties().getHandlerName());
            this.taskProcessor = new TaskProcessor(taskProcessingContext, taskProcessorMetrics);
            this.thread = THREAD_FACTORY.newThread(taskProcessor);
            this.taskProcessorMetricsFactory = taskProcessorMetricsFactory;

        }

        public void start() {
            if(thread.getState().equals(Thread.State.NEW)){
                thread.start();
            }
        }

        public void close() {
            try {
                taskProcessor.close();
                thread.interrupt();
            } catch (Exception e) {
                log.error("Error closing task processor={}", taskProcessor.getHandlerName(), e);//NOPMD
                lastException = e;
            }
        }

        public void restart() {
            if (thread.getState() == Thread.State.TERMINATED) {
                this.taskProcessor = new TaskProcessor(taskProcessingContext, taskProcessorMetrics);
                this.thread = new Thread(taskProcessor);
                thread.start();
            } else {
                log.warn("Thread is still alive, cannot restart");
            }
        }

        public String getHandlerName() {
            return taskProcessor.getHandlerName();
        }

        //This probably should go in the TaskProcessor to check that the process is alive
        public void requeueTimeoutTask() {
            log.info("Requeue timeout task started");
            TimeMeter timeMeter = taskProcessorMetrics.recordRequeueTime();
            try {
                TaskInfoService.RequeueResult requeueResult = taskProcessingContext.getTaskInfoService().requeueTimeoutTask(getHandlerName());
                timeMeter.success();
                taskProcessorMetrics.incrementTaskRequeueCount(requeueResult.updateCount());
                notifyRequeueListener(getHandlerName(), requeueResult.updateCount(), true);

                if(log.isInfoEnabled()) {
                    log.info("handlerName={}, stats={}", getHandlerName(), JsonUtil.toJson(stats()));
                }
            } catch (RuntimeException ex) {
                log.error("Requeue failing for handlerName={}", getHandlerName(), ex);//NOPMD
                timeMeter.error(ex);
                notifyRequeueListener(getHandlerName(), 0, false);
            }
        }

        public Map<String, Integer> stats() {
            return taskProcessingContext.getTaskInfoService()
                    .stats(getHandlerName(), LocalDate.now(taskProcessingContext.getClock()));
        }

        public TaskProcessor.EventPublisher getEventPublisher() {
            return taskProcessor.getEventManager();
        }

        public void notifyRequeueListener(String handlerName, int requeueCount, boolean success) {
            TaskProcessor.RequeueEndedEvent requeueEndedEvent =
                    new TaskProcessor.RequeueEndedEvent(handlerName, requeueCount, success);
            taskProcessor.getEventManager().notifyListeners(requeueEndedEvent);
        }

        public boolean isRunning() {
            return taskProcessor.isRunning();
        }

        public boolean isPaused() {
            return taskProcessor.isPaused();
        }
    }
}
