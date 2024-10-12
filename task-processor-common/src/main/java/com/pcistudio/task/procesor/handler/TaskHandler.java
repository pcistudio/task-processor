package com.pcistudio.task.procesor.handler;

import com.pcistudio.task.procesor.TaskInfo;

public interface TaskHandler {
    void process(TaskInfo<Object> task);
}
