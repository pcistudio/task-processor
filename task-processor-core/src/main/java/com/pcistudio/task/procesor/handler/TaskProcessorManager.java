package com.pcistudio.task.procesor.handler;


import com.pcistudio.task.procesor.metrics.*;
import com.pcistudio.task.procesor.util.CacheSupplier;
import com.pcistudio.task.procesor.util.DeamonThreadFactory;
import com.pcistudio.task.procesor.util.DefaultThreadFactory;
import com.pcistudio.task.procesor.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Supplier;

@Slf4j
public class TaskProcessorManager implements TaskProcessorLifecycleManager {
    private final Map<String, TaskHolder> processorMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService requeueExecutor = Executors.newScheduledThreadPool(1, new DefaultThreadFactory("task-requeue"));
    //TODO for now keep the metrics in here but this will need to be created from a factory
    //
    private final TaskProcessorMetricsFactory metricsFactory;
    private final ManagerStats managerStats = new ManagerStats();
    /**
     * Don't remove this field, it is used to keep the metrics alive
     */
    private final TaskProcessorManagerMetrics managerMetrics;
    private final Supplier<ManagerStats> managerStatsSupplier = CacheSupplier.from(this::managerStats);

    public TaskProcessorManager(final TaskProcessorMetricsFactory metricsFactory) {
        this.metricsFactory = metricsFactory;
        this.managerMetrics = this.metricsFactory.createManagerMetrics(managerStatsSupplier);
    }

    public void createTaskProcessor(final TaskProcessingContext context) {
        final String handlerName = context.getHandlerProperties().getHandlerName();
        if (this.processorMap.containsKey(handlerName)) {
            throw new IllegalStateException("Task processor with name " + handlerName + " already exists");
        }
        this.processorMap.put(handlerName, new TaskHolder(context, metricsFactory));
        final long requeueIntervalMs = context.getHandlerProperties().getRequeueIntervalMs();
        this.requeueExecutor.scheduleWithFixedDelay(this::requeueTimeoutTask, requeueIntervalMs, requeueIntervalMs, TimeUnit.MILLISECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread(this::closeRequestExecutor));

        if (context.getHandlerProperties().isAutoStartEnabled()) {
            log.info("Auto starting handlerName={}", handlerName);
            start(handlerName);
        }
    }

    private void closeRequestExecutor() {
        try {
            requeueExecutor.shutdown();
            final boolean awaitTermination = requeueExecutor.awaitTermination(10, TimeUnit.SECONDS);
            if (!awaitTermination) {
                requeueExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Error closing task processor", e);
            requeueExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void close() {
        final Iterator<Map.Entry<String, TaskHolder>> iterator = processorMap.entrySet().iterator();
        TaskHolder taskHolder;
        while (iterator.hasNext()) {
            taskHolder = iterator.next().getValue();
            taskHolder.close();
        }
        closeRequestExecutor();
    }

    @Override
    public void start() {
        for (final String handler : processorMap.keySet()) {
            start(handler);
        }
    }

    @Override
    public void close(final String handlerName) {
        processorMap.get(handlerName).close();
    }


    @Override
    public void start(final String handlerName) {
        processorMap.get(handlerName).start();
    }

    public Map<String, Integer> todayStats(final String handlerName) {
        return processorMap.get(handlerName).todayStats();
    }

    @Override
    public void restart(final String handlerName) {
        processorMap.get(handlerName).restart();
    }

    public TaskProcessor.EventPublisher getEventPublisher(final String handlerName) {
        return processorMap.get(handlerName).getEventPublisher();
    }

    // TODO fix the for and use the same idea than start
    // Do a listener or observer to be able to execute code when this is trigger
    public void requeueTimeoutTask() {
        for (final Map.Entry<String, TaskHolder> handlerNameEntry : processorMap.entrySet()) {
            final String handlerName = handlerNameEntry.getKey();
            final TaskHolder taskHolder = handlerNameEntry.getValue();
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

    private ManagerStats managerStats() {
        return managerStats
                .totalHandlers(processorMap.size())
                .runningHandlers(
                        processorMap.values().stream()
                                .filter(TaskHolder::isRunning)
                                .count()
                )
                .pausedHandlers(
                        processorMap.values().stream()
                                .filter(TaskHolder::isPaused)
                                .count()
                );
    }

    private static class TaskHolder {
        private static final ThreadFactory THREAD_FACTORY = new DeamonThreadFactory("task-processor");
        private final TaskProcessingContext context;
        private TaskProcessor taskProcessor;
        private Thread thread;
        private TaskProcessorMetrics metrics;


        public TaskHolder(final TaskProcessingContext context, final TaskProcessorMetricsFactory metricsFactory) {
            this.context = context;
            this.metrics = metricsFactory.createHandlerMetrics(context.getHandlerProperties().getHandlerName());
            this.taskProcessor = new TaskProcessor(context, metrics);
            this.thread = THREAD_FACTORY.newThread(taskProcessor);
        }

        public void start() {
            if (thread.getState().equals(Thread.State.NEW)) {
                thread.start();
            }
        }

        public void close() {
            try {
                taskProcessor.close();
                thread.interrupt();
            } catch (Exception e) {
                log.error("Error closing task processor={}", taskProcessor.getHandlerName(), e);//NOPMD
            }
        }

        public void restart() {
            if (thread.getState() == Thread.State.TERMINATED) {
                this.taskProcessor = new TaskProcessor(context, metrics);
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
            final TimeMeter timeMeter = metrics.recordRequeueTime();
            try {
                final TaskInfoService.RequeueResult requeueResult = context.getTaskInfoService().requeueTimeoutTask(getHandlerName());
                timeMeter.success();
                metrics.incrementTaskRequeueCount(requeueResult.updateCount());
                notifyRequeueListener(getHandlerName(), requeueResult.updateCount(), true);

                if (log.isInfoEnabled()) {
                    log.info("handlerName={}, today stats={}", getHandlerName(), JsonUtil.toJson(todayStats()));
                }
            } catch (RuntimeException ex) {
                log.error("Requeue failing for handlerName={}", getHandlerName(), ex);//NOPMD
                timeMeter.error(ex);
                notifyRequeueListener(getHandlerName(), 0, false);
            }
        }

        public Map<String, Integer> todayStats() {
            return context.getTaskInfoService()
                    .stats(getHandlerName(), LocalDate.now(context.getClock()));
        }

        public TaskProcessor.EventPublisher getEventPublisher() {
            return taskProcessor.getEventManager();
        }

        public void notifyRequeueListener(final String handlerName, final int requeueCount, final boolean success) {
            final TaskProcessor.RequeueEndedEvent requeueEndedEvent =
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
