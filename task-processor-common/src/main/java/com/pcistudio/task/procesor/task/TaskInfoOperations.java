package com.pcistudio.task.procesor.task;

public interface TaskInfoOperations extends TaskMetadata {

    void completed();

    void markForRetry();

    void failed();
}
