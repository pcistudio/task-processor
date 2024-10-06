package com.contact.manager.model.batch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class BatchInfoDefault implements BatchInfo<UUID, Long> {

    private final UUID batchId;
    private final List<Long> successful = new ArrayList<>();
    private final List<Long> fail = new ArrayList<>();

    public BatchInfoDefault(UUID batchId) {
        this.batchId = batchId;
    }

    public BatchInfoDefault() {
        this(UUID.randomUUID());
    }

    @Override
    public UUID getBatchId() {
        return batchId;
    }

    @Override
    public List<Long> getSuccessful() {
        return Collections.unmodifiableList(successful);
    }

    @Override
    public List<Long> getFail() {
        return Collections.unmodifiableList(fail);
    }

    @Override
    public void addSuccessful(Long successful) {
        this.successful.add(successful);
    }

    @Override
    public void addFail(Long fail) {
        this.fail.add(fail);
    }

}
