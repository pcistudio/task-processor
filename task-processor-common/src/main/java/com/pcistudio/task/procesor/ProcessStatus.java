package com.pcistudio.task.procesor;

public enum ProcessStatus {
    FAILED,// we won't retry after this
    COMPLETED,
    PENDING,
    PROCESSING, // we are processing this

    //TODO Use circuit breaker pattern if lot of retries are happening
//    RETRYING
}
