package com.contact.manager.model.batch;

import com.contact.manager.entities.BaseEntity;

import java.util.List;

public interface BatchInfo<BATCH_ID, ENTITY_ID> {

    BATCH_ID getBatchId();

    List<ENTITY_ID> getSuccessful();

    List<ENTITY_ID> getFail();

    void addSuccessful(ENTITY_ID successful);

    void addFail(ENTITY_ID fail);

    default void addSuccessful(BaseEntity<ENTITY_ID> baseEntity) {
        addSuccessful(baseEntity.getId());
    }

    default void addSuccessful(List<? extends BaseEntity> entityList) {
        entityList.forEach(this::addSuccessful);
    }
}
