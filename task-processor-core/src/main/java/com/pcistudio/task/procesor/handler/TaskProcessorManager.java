package com.pcistudio.task.procesor.handler;


import com.pcistudio.task.procesor.util.DeamonThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;

@Slf4j
public class TaskProcessorManager implements TaskProcessorLifecycleManager {
    private Map<String, TaskRunner> processorMap = new ConcurrentHashMap<>();

    public void createTaskProcessor(TaskProcessingContext taskProcessingContext) {
        TaskRunner taskProcessor = new TaskRunner(taskProcessingContext);
        if (this.processorMap.containsKey(taskProcessor.getHandlerName())) {
            throw new IllegalStateException("Task processor with name " + taskProcessor.getHandlerName() + " already exists");
        }
        this.processorMap.put(taskProcessor.getHandlerName(), new TaskRunner(taskProcessingContext));
    }

    @Override
    public void close() {
        Iterator<Map.Entry<String, TaskRunner>> iterator = processorMap.entrySet().iterator();
        TaskRunner taskRunner;
        while (iterator.hasNext()) {
            taskRunner = iterator.next().getValue();
            taskRunner.close();
        }
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

    private static class TaskRunner {
        private static final ThreadFactory THREAD_FACTORY = new DeamonThreadFactory("task-processor-");
        private final TaskProcessingContext taskProcessingContext;
        private TaskProcessor taskProcessor;
        private Thread thread;
        private Exception lastException = null;

        public TaskRunner(TaskProcessingContext taskProcessingContext) {
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
    }
}
