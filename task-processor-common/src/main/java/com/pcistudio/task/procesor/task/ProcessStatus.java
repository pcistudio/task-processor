package com.pcistudio.task.procesor.task;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum ProcessStatus {
    FAILED,// we won't retry after this
    COMPLETED,
    CANCELED,
    /* It will happen with corrupt data while reading the record*/
    CORRUPT_RECORD,
    PENDING,
    PROCESSING,
    ; // we are processing this

// TODO-0 check that always in pending status is retry and then is move to where it should go using the retry count
//    this will allow to keep the retry count without having to set it to 0
//TODO-1    this rules are for manual changes
//    everything can be move to pending if we want to retry
//    only processed or pending can be move to canceled
//    if it is in processing we should wait until expire timeout is meet
    //TODO-2 Use circuit breaker pattern if lot of retries are happening

    private static final Map<String, ProcessStatus> statusMap = Arrays.stream(values())
            .collect(Collectors.toMap(Enum::name, Function.identity()));

    public static ProcessStatus from(String status) {
        return statusMap.getOrDefault(status, CORRUPT_RECORD);
    }
}
