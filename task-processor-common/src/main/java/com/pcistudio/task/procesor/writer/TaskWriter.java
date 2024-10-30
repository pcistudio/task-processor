package com.pcistudio.task.procesor.writer;

import com.pcistudio.task.procesor.task.ProcessStatus;
import com.pcistudio.task.procesor.task.TaskInfo;
import com.pcistudio.task.procesor.task.TaskMetadata;
import com.pcistudio.task.procesor.task.TaskParams;
import com.pcistudio.task.procesor.util.encoder.MessageEncoding;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class TaskWriter {
    private final TaskInfoWriter taskInfoWriter;
    private final MessageEncoding messageEncoding;
    private final Clock clock;

    public TaskMetadata writeTasks(TaskParams taskParams) {
        TaskInfo taskInfo = createTaskInfo(UUID.randomUUID(), taskParams);
        return taskInfoWriter.writeTasks(taskInfo);
    }

    public List<TaskMetadata> writeTasks(Collection<TaskParams> taskParamsCollection) {
        UUID batchId = UUID.randomUUID();

        List<TaskInfo> taskInfos = taskParamsCollection.stream()
                .map(taskParams -> createTaskInfo(batchId, taskParams))
                .toList();
        return taskInfoWriter.writeTasks(taskInfos);
    }

    private TaskInfo createTaskInfo(UUID batchId, TaskParams taskParams) {
        Instant executionTime = taskParams.getExecutionTime();
        if (executionTime == null) {
            executionTime = Instant.now(clock);
        }
        if (taskParams.getDelay() != null) {
            executionTime = executionTime.plus(taskParams.getDelay());
        }

        return TaskInfo.builder()
                .payloadBytes(messageEncoding.encode(taskParams.getPayload()))
                .objectType(taskParams.getPayload().getClass().getCanonicalName())
                .executionTime(executionTime)
                .handlerName(taskParams.getHandlerName())
                .batchId(batchId)

                .version(1L)
                .createdAt(Instant.now(clock))
                .updatedAt(Instant.now(clock))
                .retryCount(0)
                .status(ProcessStatus.PENDING)
                .build();
    }
}
