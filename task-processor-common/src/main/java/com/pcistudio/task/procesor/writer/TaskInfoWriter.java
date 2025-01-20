package com.pcistudio.task.procesor.writer;

import com.pcistudio.task.procesor.task.TaskInfo;
import com.pcistudio.task.procesor.task.TaskMetadata;

import javax.annotation.concurrent.Immutable;
import java.util.Collection;
import java.util.List;

@Immutable
public interface TaskInfoWriter {
    TaskMetadata writeTasks(TaskInfo taskInfo);

    List<TaskMetadata> writeTasks(Collection<TaskInfo> taskInfos);
}
