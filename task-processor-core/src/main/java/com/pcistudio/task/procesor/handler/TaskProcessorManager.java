package com.pcistudio.task.procesor.handler;


import com.pcistudio.task.procesor.task.ProcessStatus;
import com.pcistudio.task.procesor.util.DeamonThreadFactory;
import com.pcistudio.task.procesor.util.DefaultThreadFactory;
import com.pcistudio.task.procesor.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@RequiredArgsConstructor
public class TaskProcessorManager implements TaskProcessorLifecycleManager {
    private Map<String, TaskHolder> processorMap = new ConcurrentHashMap<>();
    private ScheduledExecutorService requeueExecutor = Executors.newScheduledThreadPool(1, new DefaultThreadFactory("task-requeue"));

    public void createTaskProcessor(TaskProcessingContext taskProcessingContext) {
        String handlerName = taskProcessingContext.getHandlerProperties().getHandlerName();
        if (this.processorMap.containsKey(handlerName)) {
            throw new IllegalStateException("Task processor with name " + handlerName + " already exists");
        }
        this.processorMap.put(handlerName, new TaskHolder(taskProcessingContext));
        long requeueIntervalMs = taskProcessingContext.getHandlerProperties().getRequeueIntervalMs();
        this.requeueExecutor.scheduleWithFixedDelay(this::requeueTimeoutTask, requeueIntervalMs, requeueIntervalMs, TimeUnit.MILLISECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread(this::closeRequestExecutor));
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

    public void start() {
        for (String handler : processorMap.keySet()) {
            start(handler);
        }
    }

    public void close(String handlerName) {
        processorMap.get(handlerName).close();
    }

//    public void pause(String handlerName) {
//        processorMap.get(handlerName).pause();
//    }

    public void start(String handlerName) {
        processorMap.get(handlerName).start();
    }

    public void restart(String handlerName) {
        processorMap.get(handlerName).restart();
    }

    // TODO fix the for and use the same idea than start
    // Do a listener or observer to be able to execute code when this is trigger
    public void requeueTimeoutTask() {
        for (Map.Entry<String, TaskHolder> handlerNameEntry : processorMap.entrySet()) {
            String handlerName = handlerNameEntry.getKey();
            TaskHolder taskHolder = handlerNameEntry.getValue();
            try {
                taskHolder.requeueTimeoutTask();
            } catch (RuntimeException e) {
                log.error("Error requeue handlerName={}", handlerName, e);
            }
        }
    }

    private static class TaskHolder {
        private static final ThreadFactory THREAD_FACTORY = new DeamonThreadFactory("task-processor");
        private final TaskProcessingContext taskProcessingContext;
        private TaskProcessor taskProcessor;
        private Thread thread;
        private Exception lastException = null;

        public TaskHolder(TaskProcessingContext taskProcessingContext) {
            this.taskProcessingContext = taskProcessingContext;
            this.taskProcessor = new TaskProcessor(taskProcessingContext);
            this.thread = THREAD_FACTORY.newThread(taskProcessor);
        }

        public void start() {
            thread.start();
        }

//        public void pause() {
//            taskProcessor.pause();
//        }

        public void close() {
            try {
                taskProcessor.close();
                thread.interrupt();
            } catch (Exception e) {
                log.error("Error closing task processor={}", taskProcessor.getHandlerName(), e);
                lastException = e;
            }
        }

        public void restart() {
            if (thread.getState() == Thread.State.TERMINATED) {
                this.taskProcessor = new TaskProcessor(taskProcessingContext);
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
            try {
                log.info("Requeue timeout task started");
                taskProcessingContext.getTaskInfoService().requeueTimeoutTask(getHandlerName());
                notifyRequeueListener(getHandlerName(), true);
                log.info("handlerName={}, stats={}", getHandlerName(), JsonUtil.toJson(stats()));
            } catch (RuntimeException ex) {
                log.error("Requeue failing for handlerName={}", getHandlerName(), ex);
                notifyRequeueListener(getHandlerName(), false);
            }
        }

        public Map<ProcessStatus, Integer> stats() {
            return taskProcessingContext.getTaskInfoService().stats(getHandlerName(), LocalDate.now(taskProcessingContext.getClock()));
        }

        private List<RequeueListener> getRequeueListeners() {
            return taskProcessingContext.getRequeueListeners();
        }

        private void notifyRequeueListener(String handlerName, boolean success) {
            getRequeueListeners()
                    .forEach(requeueListener -> {
                                try {
                                    requeueListener.requeued(handlerName, success);
                                } catch (RuntimeException ex) {
                                    log.error("Error notifing handlerName={}", handlerName, ex);
                                }
                            }
                    );
        }
    }
}
