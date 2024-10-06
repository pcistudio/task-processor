package com.contact.manager.notification;

public enum ProcessStatus {
    FAILED,// we won't retry after this
    PROCESSED,
    PENDING,
    LOCKED, // we are processing this

    //TODO Use circuit breaker pattern if lot of retries are happening
    RETRYING
}
