package com.pcistudio.task.procesor.handler;

import com.pcistudio.task.procesor.HandlerPropertiesWrapper;
import com.pcistudio.task.procesor.metrics.TaskProcessorMetrics;
import com.pcistudio.task.procesor.metrics.TimeMeter;
import com.pcistudio.task.procesor.task.ProcessStatus;
import com.pcistudio.task.procesor.task.TaskInfo;
import com.pcistudio.task.procesor.task.TaskInfoDecoder;
import com.pcistudio.task.procesor.util.Assert;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Iterator;
import java.util.List;

import static com.pcistudio.task.procesor.util.ExceptionUtils.unwrapException;

@Slf4j
public class TaskHandlerProxy implements Iterable<TaskInfo> {

    private final TaskProcessingContext context;
    private final TaskProcessorMetrics metrics;

    public TaskHandlerProxy(final TaskProcessingContext context, final TaskProcessorMetrics metrics) {
        this.context = context;
        this.metrics = metrics;
    }

    public List<TaskInfo> poll() {
        final HandlerPropertiesWrapper properties = context.getHandlerProperties();
        final TimeMeter timeMeter = metrics.recordTaskPolling();
        try {
            final List<TaskInfo> taskInfos = context.getTaskInfoService()
                    .poll(properties.getHandlerName(), properties.getMaxPoll());

            timeMeter.success();
            return taskInfos;
        } catch (RuntimeException exception) {
            if (log.isErrorEnabled()) {
                log.error("Error polling tasks handler={}", properties.getHandlerName(), exception);
            }
            timeMeter.error(exception);
            return List.of();
        }
    }

    @Override
    public Iterator<TaskInfo> iterator() {
        return new Iterator<>() {
            private List<TaskInfo> tasks = poll();
            private Iterator<TaskInfo> tasksTempIterator = tasks.iterator();

            @Override
            public boolean hasNext() {
                if (tasksTempIterator.hasNext()) {
                    return true;
                }
                if (tasks.size() < context.getHandlerProperties().getMaxPoll()) {
                    return false;
                }
                tasks = poll();
                tasksTempIterator = tasks.iterator();
                return tasksTempIterator.hasNext();
            }

            @Override
            public TaskInfo next() {
                return tasksTempIterator.next();
            }
        };
    }

    /**
     * @param task
     */
    public void process(final TaskInfo task) {
        Assert.isTrue(task.getStatus().equals(ProcessStatus.PROCESSING), "Task is not in PROCESSING state");
        try {
            processAndUpdateStatus(wrapForDecode(task));
        } catch (TaskTransientException exception) { // library exception
            throw exception;
        } catch (RuntimeException exception) { // library exception
            log.error("Error processing task={} in handler={}", task.getId(), task.getHandlerName(), exception); //NOPMD
            if (context.isTransient(exception)) {
                throw new TaskTransientException("Task execution exception" + task.getId(), exception);
            }
            throw exception;
        }
    }

    private TaskInfoDecoder wrapForDecode(final TaskInfo task) {
        return new TaskInfoDecoder(task, context.getMessageDecoding(), context.getTaskHandlerType());
    }

    private void processAndUpdateStatus(final TaskInfoDecoder task) {
        final TimeMeter timeMeter = metrics.recordTaskProcess();
        try {
            if (log.isDebugEnabled()) {
                log.debug("Processing task={} from handler={}", task.getId(), task.getHandlerName());
            }

            doProcess(task);
            //TODO stop when the error is not related to the handler
            // like the error with the getting the handler type
            // or the
            context.getTaskInfoService().markTaskCompleted(task);
            timeMeter.success();

        } catch (TaskHandlerException exception) {// handler exception
            timeMeter.error(unwrapException(exception));//NOPMD
            context.getTaskInfoService().storeError(task.createError(exception));
            context.getTaskInfoService().markTaskFailed(task);
            log.warn("Error calling handler={} for taskId={}", context.getHandlerProperties().getHandlerName(), task.getId(), exception);//NOPMD
        } catch (TaskHandlerTransientException exception) {
            final Instant nextRetry = context.getRetryManager().nextRetry(task.getRetryCount());
            context.getTaskInfoService().markTaskToRetry(task, nextRetry);
            timeMeter.retry(unwrapException(exception));
            throw exception;
        }
    }

    private void doProcess(final TaskInfoDecoder task) throws TaskHandlerException {
        try {
            context.getTaskHandler().process(task.getPayload());
        } catch (TaskHandlerTransientException ex) {
            throw ex;
        } catch (TaskTransientException ex) {
            log.warn("Handler={} throwing TaskTransientException, TaskHandlerTransientException should be used instead", context.getHandlerProperties().getHandlerName());//NOPMD
            throw new TaskHandlerTransientException("Wrapping Transient exception", ex);
        } catch (RuntimeException exception) {
            if (shouldRetry(exception, task.getRetryCount())) {
                throw new TaskHandlerTransientException("Task execution exception", exception);
            }
            throw new TaskHandlerException("Error processing task=" + task.getId(), exception);
        }
    }

    private boolean shouldRetry(final RuntimeException exception, final int retryCount) {
        // TODO AL this int eh RetryObject on the context
        return isTransient(exception) &&
                context.getRetryManager().shouldRetry(retryCount);
    }

    private boolean isTransient(final RuntimeException exception) {
        return context.isTransient(exception);
    }

}
