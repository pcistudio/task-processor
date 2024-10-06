package com.pcistudio.task.procesor.writer;

import com.pcistudio.task.procesor.TaskInfo;

import java.util.Collection;
import java.util.List;

public interface TaskWriter {
    TaskInfo<Object> writeTasks(String handlerName, Object payload);

    List<TaskInfo<Object>> writeTasks(String handlerName, Collection<Object> payload);
}
