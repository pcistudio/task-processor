package com.pcistudio.task.procesor;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class HandlerWriteProperties {
    @Builder.Default
    private String handlerName = "default";
    @Builder.Default
    private String tableName = "default";
    @Builder.Default
    private boolean encrypt = false;


}
