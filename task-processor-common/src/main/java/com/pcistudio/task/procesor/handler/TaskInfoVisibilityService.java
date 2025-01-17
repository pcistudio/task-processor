package com.pcistudio.task.procesor.handler;

import com.pcistudio.task.procesor.page.Pageable;
import com.pcistudio.task.procesor.page.Sort;
import com.pcistudio.task.procesor.task.ProcessStatus;
import com.pcistudio.task.procesor.task.TaskInfo;

import java.time.LocalDate;
import java.util.Map;

public interface TaskInfoVisibilityService {

    Pageable<TaskInfo> getTasks(String handlerName, ProcessStatus processStatus, String pageToken, int limit, Sort sort);

    Pageable<TaskInfo> getTasksRetried(String handlerName, String pageToken, int limit);

    int count(String handlerName, LocalDate date);

    Map<String, Integer> stats(String handlerName, LocalDate date);

    default int calculateFinishedTask(String handlerName, LocalDate date) {
        Map<String, Integer> stats = stats(handlerName, date);
        return stats.getOrDefault(ProcessStatus.COMPLETED.name(), 0) + stats.getOrDefault(ProcessStatus.FAILED.name(), 0);
    }

    //Useful for integration test
    default boolean allTaskCompleted(String handlerName, LocalDate date) {
        count(handlerName, date);
        return count(handlerName, date) == calculateFinishedTask(handlerName, date);
    }
}
