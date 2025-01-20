package com.pcistudio.task.procesor.handler;


import com.pcistudio.task.procesor.HandlerPropertiesWrapper;
import com.pcistudio.task.procesor.metrics.ProcessorStats;
import com.pcistudio.task.procesor.metrics.TaskProcessorMetrics;
import com.pcistudio.task.procesor.task.TaskInfo;
import com.pcistudio.task.procesor.util.*;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
public final class TaskProcessor implements Closeable, Runnable {
    private final TaskHandlerProxy taskHandlerProxy;
    private final HandlerPropertiesWrapper properties;
    private final TaskProcessingContext context;
    private final AtomicReference<TaskProcessorState> state = new AtomicReference<>(TaskProcessorState.CREATED);
    private final Consumer<TaskInfo> taskHandler;
    private final EventManager eventManager;
    private final TaskProcessorMetrics metrics;
    private final ExecutionTracker executionTracker;
    private final Supplier<ProcessorStats> processorStatsSupplier = CacheSupplier.from(this::getStats);

    /* package */
    TaskProcessor(final TaskProcessingContext context, final TaskProcessorMetrics metrics) {
        this(
                context,
                metrics,
                new TaskHandlerProxy(context, metrics),
                new ThreadPoolExecutor(
                        Math.max(context.getHandlerProperties().getMaxParallelTasks() / 2, 1),
                        context.getHandlerProperties().getMaxParallelTasks(),
                        1000L,
                        TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<>(Math.max(context.getHandlerProperties().getMaxParallelTasks(), context.getHandlerProperties().getMaxPoll()) * 2),
                        new DefaultThreadFactory("task-handler-" + context.getHandlerProperties().getHandlerName()),
                        new BlockingRejectedExecutionHandler()
                )
        );
    }

    /* package */
    TaskProcessor(final TaskProcessingContext context, final TaskProcessorMetrics metrics, final TaskHandlerProxy taskHandlerProxy, final ThreadPoolExecutor executorService) {
        this.context = context;
        this.metrics = metrics;
        this.taskHandlerProxy = taskHandlerProxy;
        this.properties = context.getHandlerProperties();

        this.taskHandler = context.getCircuitBreaker().decorate(taskHandlerProxy::process);

        this.eventManager = new DefaultEventManager();
        this.executionTracker = new TaskExecutionTracker(properties.getHandlerName(), executorService, context.getClock(), properties.getLongTaskTimeMs(), properties.getLongTaskCheckIntervalMs(), properties.getLongTaskCheckInitialDelayMs());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                close();
            } catch (RuntimeException e) {
                log.error("Error closing task processor={}", properties.getHandlerName(), e);
            }
        }));

        metrics.registerProcessor(processorStatsSupplier);
        log.info("Created task processor for handler={}", properties.getHandlerName());//NOPMD
    }

    public void processTasks() throws InterruptedException, TaskProcessorClosingException {
        changeCurrentState(TaskProcessorState.RUNNING);

        if (log.isInfoEnabled()) {
            log.info("Task processor={} starting", getHandlerName());
        }

        while (notShuttingDown()) {
            try {
                doProcessTasks(taskHandlerProxy.iterator());
                waitForNextPoll();
            } catch (TaskProcessorPauseException e) {
                log.warn("Task processor={} paused", getHandlerName(), e);
                waitForCircuitToOpen();
                state.compareAndSet(TaskProcessorState.PAUSED, TaskProcessorState.RUNNING);
                if (log.isInfoEnabled()) {
                    log.info("Task processor={} change from PAUSED to RUNNING", getHandlerName());
                }
            }
        }

        if (log.isInfoEnabled()) {
            log.info("Task processor={} exiting with state={}", getHandlerName(), state.get().name());
        }
    }

    private void waitForCircuitToOpen() throws InterruptedException {
        notifyCircuitWaiting(properties.getHandlerName(), properties.getPollInterval());
        Thread.sleep(properties.getPollInterval());
        notifyCircuitWaitingEnded(properties.getHandlerName());
    }

    private void waitForNextPoll() throws InterruptedException {
        if (log.isDebugEnabled()) {
            log.debug("Waiting for tasks for processor={}", getHandlerName());
        }

        notifyPollWaiting(properties.getHandlerName(), properties.getPollInterval());
        Thread.sleep(properties.getPollInterval());
        notifyPollWaitingEnded(properties.getHandlerName());
    }

    private boolean changeCurrentState(final TaskProcessorState nextState) {
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
                close();
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

    public boolean notStarted() {
        return state.get() == TaskProcessorState.CREATED;
    }

    public boolean isShuttingDown() {
        return state.get() == TaskProcessorState.SHUTTING_DOWN;
    }

    public boolean notShuttingDown() {
        return state.get() != TaskProcessorState.SHUTTING_DOWN;
    }

    public boolean isRunning() {
        return state.get() == TaskProcessorState.RUNNING;
    }

    private void doProcessTasks(final Iterator<TaskInfo> tasks) throws TaskProcessorClosingException, TaskProcessorPauseException {
        int count = 0; //TODO remove this counter
        while (tasks.hasNext()) {
            final TaskInfo task = tasks.next();
            if (isShuttingDown()) {
                log.warn("Task processor={} is shutdown, skipping task={}", getHandlerName(), task.getId());
                throw new TaskProcessorClosingException("Task processor=" + getHandlerName() + " is shutting down");
            }
            if (isPaused()) {
                log.warn("Task processor={} is paused, skipping task={}", getHandlerName(), task.getId());
                throw new TaskProcessorPauseException("Task processor=" + getHandlerName() + " is paused");
            }

            try {
                executionTracker.trackFuture(task.getId(), () -> wrapInCircuitBreaker(task));
                count++;
            } catch (RejectedExecutionException e) {
                log.error("Task processor={} rejected task={}", getHandlerName(), task.getId(), e);
            }
        }
        notifyProcessingBatch(properties.getHandlerName(), count);
    }

    private void wrapInCircuitBreaker(final TaskInfo taskInfo) {
        try {
            taskHandler.accept(taskInfo);
        } catch (TaskTransientException ignored) {

        } catch (CallNotPermittedException ex) {
            pause();
        }
    }

    @Override
    public void close() {
        shutdown();
        executionTracker.shutdown();
        if (log.isInfoEnabled()) {
            log.info("Processor {} closed", getHandlerName());
        }
    }

    public void shutdown() {
        changeCurrentState(TaskProcessorState.SHUTTING_DOWN);
    }

    private void pause() {
        changeCurrentState(TaskProcessorState.PAUSED);
        if (log.isInfoEnabled()) {
            log.info("Pausing {} closed", getHandlerName());
        }
    }

    public String getHandlerName() {
        return properties.getHandlerName();
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    @Override
    public void run() {
        try {
            processTasks();
        } catch (TaskProcessorClosingException e) {

            log.warn("Task processor={} closing", getHandlerName(), e);
        } catch (Exception e) {
            log.error("Error processing task from processor={}", getHandlerName(), e);
        } finally {
            close();
        }
    }

    private void notifyPollWaiting(final String handlerName, final long expectedWaiting) {
        eventManager.notifyListeners(new PollWaitingEvent(handlerName, expectedWaiting));
    }

    private void notifyPollWaitingEnded(final String handlerName) {
        eventManager.notifyListeners(new PollWaitingEndedEvent(handlerName));
    }

    private void notifyCircuitWaiting(final String handlerName, final long expectedWaiting) {
        eventManager.notifyListeners(new CircuitBreakerWaitingEvent(handlerName, expectedWaiting));
    }

    private void notifyCircuitWaitingEnded(final String handlerName) {
        eventManager.notifyListeners(new CircuitBreakerWaitingEndedEvent(handlerName));
    }

    private void notifyProcessingBatch(final String handlerName, final int count) {
        eventManager.notifyListeners(new ProcessingBatchEvent(handlerName, count));
    }

    private ProcessorStats getStats() {
        return new ProcessorStats(
                executionTracker.getTaskCount(),
                executionTracker.getLongWaitingTaskCount(),
                context.getCircuitBreaker().isClosed() ? 1 : 0
        );
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
        public void onRequeueEnded(final Consumer<RequeueEndedEvent> action) {
            eventListenersMap.compute(RequeueEndedEvent.class, (aClass, consumers) -> {
                final List<Consumer> result = Objects.requireNonNullElseGet(consumers, ArrayList::new);
                result.add(action);
                return result;
            });
        }

        @Override
        public void onPollWaiting(final Consumer<PollWaitingEvent> action) {
            eventListenersMap.compute(PollWaitingEvent.class, (aClass, consumers) -> {
                final List<Consumer> result = Objects.requireNonNullElseGet(consumers, ArrayList::new);
                result.add(action);
                return result;
            });
        }

        @Override
        public void onPollWaitingEnded(final Consumer<PollWaitingEndedEvent> action) {
            eventListenersMap.compute(PollWaitingEndedEvent.class, (aClass, consumers) -> {
                final List<Consumer> result = Objects.requireNonNullElseGet(consumers, ArrayList::new);
                result.add(action);
                return result;
            });
        }

        @Override
        public void onCircuitBreakerWaiting(final Consumer<CircuitBreakerWaitingEvent> action) {
            eventListenersMap.compute(CircuitBreakerWaitingEvent.class, (aClass, consumers) -> {
                final List<Consumer> result = Objects.requireNonNullElseGet(consumers, ArrayList::new);
                result.add(action);
                return result;
            });
        }

        @Override
        public void onCircuitBreakerWaitingEnded(final Consumer<CircuitBreakerWaitingEndedEvent> action) {
            eventListenersMap.compute(CircuitBreakerWaitingEndedEvent.class, (aClass, consumers) -> {
                final List<Consumer> result = Objects.requireNonNullElseGet(consumers, ArrayList::new);
                result.add(action);
                return result;
            });
        }

        @Override
        public void onProcessingBatch(final Consumer<ProcessingBatchEvent> action) {
            eventListenersMap.compute(ProcessingBatchEvent.class, (aClass, consumers) -> {
                final List<Consumer> result = Objects.requireNonNullElseGet(consumers, ArrayList::new);
                result.add(action);
                return result;
            });
        }

        @Override
        public void notifyListeners(final ProcessorEvent event) {
            Assert.notNull(event, "Event cannot be null");
            final List<Consumer> listeners = eventListenersMap.get(event.getClass());
            if (listeners == null) {
                if (log.isDebugEnabled()) {
                    log.debug("No event registered with class={}", event.getClass().getCanonicalName());
                }
                return;
            }

            listeners.forEach(consumer -> {
                try {
                    consumer.accept(event);
                } catch (Exception ex) {
                    if (log.isDebugEnabled()) {
                        log.debug("Error calling listener for class={}", event.getClass().getName(), ex);
                    }
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