package com.pcistudio.task.procesor.handler;

import com.pcistudio.task.procesor.ProcessStatus;
import com.pcistudio.task.procesor.TaskInfo;
import com.pcistudio.task.procesor.util.Assert;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;


@Slf4j
public class TaskHandlerProxy implements TaskHandler {

    private final TaskProcessingContext context;

    public TaskHandlerProxy(TaskProcessingContext taskProcessingContext) {
        this.context = taskProcessingContext;
    }

    //    @Transactional
    public void process(TaskInfo<Object> task) {
        Assert.isTrue(task.getStatus().equals(ProcessStatus.PROCESSING), "Task is not in PROCESSING state");
        try {
            processAndUpdateStatus(task);
        } catch (RuntimeException exception) { // library exception
            log.error("Error processing task", exception); // probably I don't need to do anything just rethrow it
        }
    }

    private void processAndUpdateStatus(TaskInfo<Object> task) {
        try {
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

    private void doProcess(TaskInfo<Object> task) throws TaskHandlerException {
        try {
            context.getTaskHandler().process(task);
        } catch (RuntimeException exception) {
            throw new TaskHandlerException("Error processing task", exception);
        }
    }

    private boolean shouldRetry(Throwable exception, int retryCount) {
        // TODO AL this int eh RetryObject on the context
        return context.getTransientExceptions().contains(exception) &&
                context.getRetryManager().shouldRetry(retryCount);
    }
}
