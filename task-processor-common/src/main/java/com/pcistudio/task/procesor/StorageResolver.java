package com.pcistudio.task.procesor;

public interface StorageResolver {
    String resolveStorageName(String handlerName);
    String resolveErrorStorageName(String handlerName);

    IdentityStorageResolver IDENTITY = new IdentityStorageResolver();

    final class IdentityStorageResolver implements StorageResolver {
        private IdentityStorageResolver() {
        }

        @Override
        public String resolveStorageName(String handlerName) {
            return handlerName;
        }

        @Override
        public String resolveErrorStorageName(String handlerName) {
            return handlerName + "_error";
        }
    }
}
