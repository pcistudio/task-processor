package com.pcistudio.processor.test.handler;

import com.pcistudio.task.procesor.handler.TaskInfoService;
import com.pcistudio.task.procesor.task.ProcessStatus;
import com.pcistudio.task.procesor.task.TaskInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class TaskInfoServiceTestHelper {
    private final TaskInfoService taskInfoService;
    private final Clock clock;

    public void waitForTask(String handlerName) throws InterruptedException {
        LocalDate now = LocalDate.now(clock);
        int total = taskInfoService.count(handlerName, now);
        int finished = taskInfoService.calculateFinishedTask(handlerName, now);
        Map<String, Integer> stats;
        while (total != finished) {
            Thread.sleep(1000);
            finished = taskInfoService.calculateFinishedTask(handlerName, now);
        }
    }

    public void printStats(String handlerName) {
        Map<String, Integer> stats = taskInfoService.stats(handlerName, LocalDate.now(clock));
       log.info("stats: {}", stats);
    }


    public void printTasks(List<TaskInfo> taskInfos) {
        taskInfos.forEach(taskInfo -> {
            log.info("{}\t{}\t{}", taskInfo.getId(), taskInfo.getStatus(), taskInfo.getRetryCount());
        });
    }
}
