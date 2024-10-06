package com.contact.manager.notification;

import java.util.List;
import java.util.UUID;

public class BatchResponse {
    private UUID batchId;
    private List<Long> successfulNotifications;
    private List<Long> failNotifications;

    public BatchResponse(UUID batchId, List<Long> successfulNotifications, List<Long> failNotifications) {
        this.batchId = batchId;
        this.successfulNotifications = successfulNotifications;
        this.failNotifications = failNotifications;
    }

    public UUID getBatchId() {
        return batchId;
    }

    public List<Long> getSuccessfulNotifications() {
        return successfulNotifications;
    }

    public List<Long> getFailNotifications() {
        return failNotifications;
    }
}
