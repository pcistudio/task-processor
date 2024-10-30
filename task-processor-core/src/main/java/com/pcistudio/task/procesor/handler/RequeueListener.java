package com.pcistudio.task.procesor.handler;

import lombok.Builder;
import lombok.Getter;

public interface RequeueListener {
    void requeued(RequeueEvent requeueEvent);


    default void requeued(String handlerName, boolean success) {
        requeued(RequeueEvent.builder()
                .success(success)
                .handlerName(handlerName)
                .build());
    }

    @Builder
    @Getter
    class RequeueEvent {
        private String handlerName;
        private boolean success = true;
    }

}
