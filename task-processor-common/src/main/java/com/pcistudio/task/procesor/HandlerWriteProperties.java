package com.pcistudio.task.procesor;

import lombok.Getter;

@Getter
public class HandlerWriteProperties {

    private final String handlerName;
    //TODO if tableName is null use handlerName and if handlerName is null use default
    // si default for tableName is not needed
    private final String tableName;

    private final boolean encrypt;

    protected HandlerWriteProperties(HandlerWritePropertiesBuilder<?> builder) {
        this.handlerName = builder.handlerName;
        this.tableName = builder.tableName;
        this.encrypt = builder.encrypt;
    }

    public static HandlerWritePropertiesBuilder<?> builder() {
        return new HandlerWritePropertiesBuilder<>();
    }

    public static class HandlerWritePropertiesBuilder<T extends HandlerWritePropertiesBuilder<T>> {
        private String tableName = "default";
        private String handlerName = "default";
        private boolean encrypt = false;

        public T tableName(String tableName) {
            this.tableName = tableName;
            return (T) this;
        }

        public T handlerName(String handlerName) {
            this.handlerName = handlerName;
            return (T) this;
        }

        public T encrypt(boolean encrypt) {
            this.encrypt = encrypt;
            return (T) this;
        }

        public HandlerWriteProperties build() {
            return new HandlerWriteProperties(this);
        }
    }

}
