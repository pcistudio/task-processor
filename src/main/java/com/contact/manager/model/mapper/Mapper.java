package com.contact.manager.model.mapper;

public interface Mapper<T, U> {
    U map(T t);
}
