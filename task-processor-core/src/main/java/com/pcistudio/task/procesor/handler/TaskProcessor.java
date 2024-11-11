package com.pcistudio.task.procesor.handler;


import com.pcistudio.task.procesor.HandlerPropertiesWrapper;
import com.pcistudio.task.procesor.task.TaskInfo;
import com.pcistudio.task.procesor.util.Assert;
import com.pcistudio.task.procesor.util.BlockingRejectedExecutionHandler;
import com.pcistudio.task.procesor.util.DefaultThreadFactory;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@Slf4j
public class TaskProcessor implements Closeable, Runnable {
    private final TaskHandlerProxy taskHandlerProxy;
    private final ThreadPoolExecutor executorService;
    private final HandlerPropertiesWrapper properties;
    private final TaskProcessingContext context;
    private final AtomicReference<TaskProcessorState> state = new AtomicReference<>(TaskProcessorState.CREATED);
    private final Consumer<TaskInfo> circuitBreakerTaskProcessor;
    private final EventManager eventManager;

    TaskProcessor(TaskProcessingContext taskProcessingContext) {
        this(
                taskProcessingContext,
                new TaskHandlerProxy(taskProcessingContext),
                new ThreadPoolExecutor(
                        Math.max(taskProcessingContext.getHandlerProperties().getMaxParallelTasks() / 2, 1),
                        taskProcessingContext.getHandlerProperties().getMaxParallelTasks(),
                        1000L,
                        TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<>(Math.max(taskProcessingContext.getHandlerProperties().getMaxParallelTasks(), taskProcessingContext.getHandlerProperties().getMaxPoll()) * 2),
                        new DefaultThreadFactory("task-handler-" + taskProcessingContext.getHandlerProperties().getHandlerName()),
                        new BlockingRejectedExecutionHandler()
                )
        );
    }

    TaskProcessor(TaskProcessingContext taskProcessingContext, TaskHandlerProxy taskHandlerProxy, ThreadPoolExecutor executorService) {
        this.context = taskProcessingContext;
        this.taskHandlerProxy = taskHandlerProxy;
        this.properties = taskProcessingContext.getHandlerProperties();
        this.executorService = executorService;

        this.circuitBreakerTaskProcessor = context.getCircuitBreakerDecorator().decorate(taskHandlerProxy::process);

        this.eventManager = new DefaultEventManager();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                close();
                waitTermination(10, TimeUnit.SECONDS);
            } catch (RuntimeException e) {
                log.error("Error closing task processor", e);
            }
        }));
        log.info("Created task processor for handler={}", context.getHandlerProperties().getHandlerName());
    }

    public void processTasks() throws InterruptedException, TaskProcessorClosingException {
        changeCurrentState(TaskProcessorState.RUNNING);
        log.info("Task processor={} starting", properties.getHandlerName());
        while (isRunning()) {
            try {
                doProcessTasks(taskHandlerProxy.iterator());
                waitForNextPoll();
            } catch (TaskProcessorPauseException e) {
                log.warn("Task processor={} paused", properties.getHandlerName(), e);
                waitForCircuitToOpen();
                state.compareAndSet(TaskProcessorState.PAUSED, TaskProcessorState.RUNNING);
                log.info("Task processor={} change from PAUSED to RUNNING", properties.getHandlerName());
            }
        }
    }

    private void waitForCircuitToOpen() throws InterruptedException {
        notifyCircuitWaiting(properties.getHandlerName(), properties.getPollInterval());
        Thread.sleep(properties.getPollInterval());
        notifyCircuitWaitingEnded(properties.getHandlerName());
    }

    private void waitForNextPoll() throws InterruptedException {
        log.debug("Waiting for tasks for processor={}", properties.getHandlerName());
        notifyPollWaiting(properties.getHandlerName(), properties.getPollInterval());
        Thread.sleep(properties.getPollInterval());
        notifyPollWaitingEnded(properties.getHandlerName());
    }

    private boolean changeCurrentState(TaskProcessorState nextState) {
        if (state.get() == nextState) {
            log.warn("Trying to change to the current state, processor={}", properties.getHandlerName());
            return true;
        }
        if (state.get() == TaskProcessorState.CREATED && nextState == TaskProcessorState.RUNNING) {
            return state.compareAndSet(TaskProcessorState.CREATED, nextState);
        } else if (state.get() == TaskProcessorState.RUNNING && nextState == TaskProcessorState.PAUSED) {
            return state.compareAndSet(TaskProcessorState.RUNNING, nextState);
        } else if (state.get() == TaskProcessorState.PAUSED && nextState == TaskProcessorState.SHUTTING_DOWN) {
            if (state.compareAndSet(TaskProcessorState.PAUSED, nextState)) {
                waitTermination(10, TimeUnit.SECONDS);
                return true;
            }
            return false;
        } else if (state.get() == TaskProcessorState.PAUSED && nextState == TaskProcessorState.RUNNING) {
            return state.compareAndSet(TaskProcessorState.PAUSED, nextState);
        } else if (state.get() == TaskProcessorState.CREATED && nextState == TaskProcessorState.SHUTTING_DOWN) {
            return state.compareAndSet(TaskProcessorState.CREATED, nextState);
        } else if (state.get() == TaskProcessorState.RUNNING && nextState == TaskProcessorState.SHUTTING_DOWN) {
            return state.compareAndSet(TaskProcessorState.RUNNING, nextState);
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

    private void doProcessTasks(Iterator<TaskInfo> tasks) throws TaskProcessorClosingException, TaskProcessorPauseException {
        int count = 0;
        while (tasks.hasNext()) {
            TaskInfo task = tasks.next();
            if (isShuttingDown()) {
                log.warn("Task processor={} is shutdown, skipping task={}", properties.getHandlerName(), task.getId());
                throw new TaskProcessorClosingException("Task processor=" + properties.getHandlerName() + " is shutting down");
            }
            if (isPaused()) {
                log.warn("Task processor={} is paused, skipping task={}", properties.getHandlerName(), task.getId());
                throw new TaskProcessorPauseException("Task processor=" + properties.getHandlerName() + " is shutting down");
            }

            try {
                executorService.submit(() -> wrapInCircuitBreaker(task));
                count++;
            } catch (RejectedExecutionException e) {
                log.error("Task processor={} rejected task={}", properties.getHandlerName(), task.getId(), e);
            }
        }
        notifyProcessingBatch(properties.getHandlerName(), count);
    }

    private void wrapInCircuitBreaker(TaskInfo taskInfo) {
        try {
            circuitBreakerTaskProcessor.accept(taskInfo);
        } catch (TaskTransientException ignored) {

        } catch (CallNotPermittedException ex) {
            pause();
        }
    }

    @Override
    public void close() {
        shutdown();
        waitTermination(10, TimeUnit.SECONDS);
        log.info("Processor {} closed", properties.getHandlerName());
    }

    public void shutdown() {
        changeCurrentState(TaskProcessorState.SHUTTING_DOWN);
    }

    private void pause() {
        changeCurrentState(TaskProcessorState.PAUSED);
        log.info("Pausing {} closed", properties.getHandlerName());
    }

    private void waitTermination(long timeout, TimeUnit unit) {
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

    public EventManager getEventManager() {
        return eventManager;
    }

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
        SHUTTING_DOWN,
        /**
         * It is used to pause the processor when there are to many errors
         */
        PAUSED
    }

    private void notifyPollWaiting(String handlerName, long expectedWaiting) {
        eventManager.notifyListeners(new PollWaitingEvent(handlerName, expectedWaiting));
    }

    private void notifyPollWaitingEnded(String handlerName) {
        eventManager.notifyListeners(new PollWaitingEndedEvent(handlerName));
    }

    private void notifyCircuitWaiting(String handlerName, long expectedWaiting) {
        eventManager.notifyListeners(new CircuitBreakerWaitingEvent(handlerName, expectedWaiting));
    }

    private void notifyCircuitWaitingEnded(String handlerName) {
        eventManager.notifyListeners(new CircuitBreakerWaitingEndedEvent(handlerName));
    }

    private void notifyProcessingBatch(String handlerName, int count) {
        eventManager.notifyListeners(new ProcessingBatchEvent(handlerName, count));
    }


    public interface EventPublisher {
        void onRequeueEnded(Consumer<RequeueEndedEvent> action);

        void onPollWaiting(Consumer<PollWaitingEvent> action);

        void onPollWaitingEnded(Consumer<PollWaitingEndedEvent> action);

        void onCircuitBreakerWaiting(Consumer<CircuitBreakerWaitingEvent> action);

        void onCircuitBreakerWaitingEnded(Consumer<CircuitBreakerWaitingEndedEvent> action);

        void onProcessingBatch(Consumer<ProcessingBatchEvent> action);
    }

    public interface EventTrigger {
        void notifyListeners(ProcessorEvent event);
    }

    public interface ProcessorEvent {
    }

    public interface EventManager extends EventPublisher, EventTrigger {
    }

    @Slf4j
    public static class DefaultEventManager implements EventManager {
        private Map<Class, List<Consumer>> eventListenersMap = new ConcurrentHashMap<>();


        @Override
        public void onRequeueEnded(Consumer<RequeueEndedEvent> action) {
            eventListenersMap.compute(RequeueEndedEvent.class, (aClass, consumers) -> {
                List<Consumer> result = Objects.requireNonNullElseGet(consumers, ArrayList::new);
                result.add(action);
                return result;
            });
        }

        @Override
        public void onPollWaiting(Consumer<PollWaitingEvent> action) {
            eventListenersMap.compute(PollWaitingEvent.class, (aClass, consumers) -> {
                List<Consumer> result = Objects.requireNonNullElseGet(consumers, ArrayList::new);
                result.add(action);
                return result;
            });
        }

        @Override
        public void onPollWaitingEnded(Consumer<PollWaitingEndedEvent> action) {
            eventListenersMap.compute(PollWaitingEndedEvent.class, (aClass, consumers) -> {
                List<Consumer> result = Objects.requireNonNullElseGet(consumers, ArrayList::new);
                result.add(action);
                return result;
            });
        }

        @Override
        public void onCircuitBreakerWaiting(Consumer<CircuitBreakerWaitingEvent> action) {
            eventListenersMap.compute(CircuitBreakerWaitingEvent.class, (aClass, consumers) -> {
                List<Consumer> result = Objects.requireNonNullElseGet(consumers, ArrayList::new);
                result.add(action);
                return result;
            });
        }

        @Override
        public void onCircuitBreakerWaitingEnded(Consumer<CircuitBreakerWaitingEndedEvent> action) {
            eventListenersMap.compute(CircuitBreakerWaitingEndedEvent.class, (aClass, consumers) -> {
                List<Consumer> result = Objects.requireNonNullElseGet(consumers, ArrayList::new);
                result.add(action);
                return result;
            });
        }

        @Override
        public void onProcessingBatch(Consumer<ProcessingBatchEvent> action) {
            eventListenersMap.compute(ProcessingBatchEvent.class, (aClass, consumers) -> {
                List<Consumer> result = Objects.requireNonNullElseGet(consumers, ArrayList::new);
                result.add(action);
                return result;
            });
        }

        public void notifyListeners(ProcessorEvent event) {
            Assert.notNull(event, "Event cannot be null");
            List<Consumer> listeners = eventListenersMap.get(event.getClass());
            if (listeners == null) {
                log.debug("No event registered with class={}", event.getClass().getCanonicalName());
                return;
            }

            listeners.forEach(consumer -> {
                try {
                    consumer.accept(event);
                } catch (Exception ex) {
                    log.debug("Error calling listener for class={}", event.getClass().getName(), ex);
                }
            });
        }
    }


    public record RequeueEndedEvent(String handlerName, int requeueCount, boolean success) implements ProcessorEvent {
    }

    public record PollWaitingEvent(String handlerName, long expectedWaiting) implements ProcessorEvent {
    }

    public record PollWaitingEndedEvent(String handlerName) implements ProcessorEvent {
    }

    public record CircuitBreakerWaitingEvent(String handlerName, long expectedWaiting) implements ProcessorEvent {
    }

    public record CircuitBreakerWaitingEndedEvent(String handlerName) implements ProcessorEvent {
    }

    public record ProcessingBatchEvent(String handlerName, long processingCount) implements ProcessorEvent {
    }
}