package com.pcistudio.task.procesor;

import com.pcistudio.task.procesor.util.Assert;
import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.Getter;

import java.util.Objects;

@Getter
public class HandlerWriteProperties {

    private final String handlerName;

    private final String tableName;

    private final boolean encrypt;

    protected HandlerWriteProperties(HandlerWritePropertiesBuilder<?> builder) {

        this.handlerName = Objects.requireNonNullElse(builder.handlerName, builder.tableName);
        this.tableName = Objects.requireNonNullElse(builder.tableName, builder.handlerName);
        this.encrypt = builder.encrypt;
    }

    public static HandlerWritePropertiesBuilder<?> builder() {
        return new HandlerWritePropertiesBuilder<>();
    }

    public static class HandlerWritePropertiesBuilder<T extends HandlerWritePropertiesBuilder<T>> {
        @Nullable
        private String tableName;
        @Nullable
        private String handlerName;
        private boolean encrypt = false;

        public T tableName(String tableName) {
            Assert.notNull(tableName, "tableName cannot be null");
            Assert.isFalse(tableName.isBlank(), "tableName cannot be empty");
            this.tableName = tableName;
            return (T) this;
        }

        public T handlerName(String handlerName) {
            Assert.notNull(handlerName, "handlerName cannot be null");
            Assert.isFalse(handlerName.isBlank(), "handlerName cannot be empty");
            this.handlerName = handlerName;
            return (T) this;
        }

        public T encrypt(boolean encrypt) {
            this.encrypt = encrypt;
            return (T) this;
        }

        public HandlerWriteProperties build() {
            checkRequiredFields();
            return new HandlerWriteProperties(this);
        }

        protected void checkRequiredFields() {
            if (tableName == null && handlerName == null) {
                throw new IllegalArgumentException("tableName or handlerName must be set");
            }
        }
    }

}
