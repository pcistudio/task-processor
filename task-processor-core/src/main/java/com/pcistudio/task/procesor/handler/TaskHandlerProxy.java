package com.pcistudio.task.procesor.handler;

import com.pcistudio.task.procesor.HandlerPropertiesWrapper;
import com.pcistudio.task.procesor.task.ProcessStatus;
import com.pcistudio.task.procesor.task.TaskInfo;
import com.pcistudio.task.procesor.task.TaskInfoDecoder;
import com.pcistudio.task.procesor.util.Assert;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.List;

@Slf4j
public class TaskHandlerProxy {

    private final TaskProcessingContext context;

    public TaskHandlerProxy(TaskProcessingContext taskProcessingContext) {
        this.context = taskProcessingContext;
    }

    public List<TaskInfo> poll() {
        HandlerPropertiesWrapper properties = context.getHandlerProperties();
        try {
            return context.getTaskInfoService().poll(properties.getHandlerName(), properties.getMaxPoll());
        } catch (RuntimeException exception) {
            log.error("Error polling tasks", exception);
            if (context.isTransient(exception)) {
                return List.of();
            }
            throw exception;
        }

    }

    //    @Transactional
    public void process(TaskInfo task) {
        Assert.isTrue(task.getStatus().equals(ProcessStatus.PROCESSING), "Task is not in PROCESSING state");
        try {
            processAndUpdateStatus(wrapForDecode(task));
        } catch (RuntimeException exception) { // library exception
            log.error("Error processing task", exception); // probably I don't need to do anything just rethrow it
        }
    }

    private TaskInfoDecoder wrapForDecode(TaskInfo task) {
        return new TaskInfoDecoder(task, context.getMessageDecoding(), context.getTaskHandlerType());
    }

    private void processAndUpdateStatus(TaskInfoDecoder task) {
        try {
            log.debug("Processing task={} from handler={}", task.getId(), task.getHandlerName());
            doProcess(task);
            context.getTaskInfoService().markTaskCompleted(task);
        } catch (TaskHandlerException exception) {// handler exception
            context.getTaskInfoService().storeError(task.createError(exception));
            if (shouldRetry(exception.getCause(), task.getRetryCount())) {
                Instant nextRetry = context.getRetryManager().nextRetry(task.getRetryCount());
                context.getTaskInfoService().markTaskToRetry(task, nextRetry);
                return;
            }
            context.getTaskInfoService().markTaskFailed(task);
        }
    }

    private void doProcess(TaskInfoDecoder task) throws TaskHandlerException {
        try {
            context.getTaskHandler().process(task.getPayload());
        } catch (RuntimeException exception) {
            //TODO improve message
            throw new TaskHandlerException("Error processing task", exception);
        }
    }

    private boolean shouldRetry(Throwable exception, int retryCount) {
        // TODO AL this int eh RetryObject on the context
        return context.isTransient((RuntimeException) exception) &&
                context.getRetryManager().shouldRetry(retryCount);
    }

}
