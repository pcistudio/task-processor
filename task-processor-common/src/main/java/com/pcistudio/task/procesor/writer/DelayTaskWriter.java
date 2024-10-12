package com.pcistudio.task.procesor.writer;

import com.pcistudio.task.procesor.TaskInfo;

import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.util.Collection;
import java.util.List;

public interface DelayTaskWriter extends TaskWriter {
    TaskInfo<Object> writeTasks(String handlerName, Object payload, Instant executionTime);

    List<TaskInfo<Object>> writeTasks(String handlerName, Collection<Object> payload, Instant executionTime);

    default List<TaskInfo<Object>> writeTasks(String handlerName, Collection<Object> payload) {
        return writeTasks(handlerName, payload, Instant.now());
    }

    default TaskInfo<Object> writeTasks(String handlerName, Object payload) {
        return writeTasks(handlerName, payload, Instant.now());
    }

    default List<TaskInfo<Object>> writeTasks(String handlerName, Collection<Object> payload, Period delay) {
        return writeTasks(handlerName, payload, Instant.now().plus(delay));
    }

    default List<TaskInfo<Object>> writeTasks(String handlerName, Collection<Object> payload, Duration delay) {
        return writeTasks(handlerName, payload, Instant.now().plus(delay));
    }

    default TaskInfo<Object> writeTasks(String handlerName, Object payload, Period delay) {
        return writeTasks(handlerName, payload, Instant.now().plus(delay));
    }

    default TaskInfo<Object> writeTasks(String handlerName, Object payload, Duration delay) {
        return writeTasks(handlerName, payload, Instant.now().plus(delay));
    }
}
