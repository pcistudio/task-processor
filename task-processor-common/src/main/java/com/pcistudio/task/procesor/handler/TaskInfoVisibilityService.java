package com.pcistudio.task.procesor.handler;

import com.pcistudio.task.procesor.page.Pageable;
import com.pcistudio.task.procesor.page.Sort;
import com.pcistudio.task.procesor.task.ProcessStatus;
import com.pcistudio.task.procesor.task.TaskInfo;

import java.time.LocalDate;
import java.util.Map;

public interface TaskInfoVisibilityService {

    Pageable<TaskInfo> getTasks(String handlerName, ProcessStatus processStatus, String pageToken, int limit, Sort sort);


    Map<ProcessStatus, Integer> stats(String handlerName, LocalDate date);
}
