package com.gitee.spring.domain.proxy.entity;

import lombok.Data;

@Data
public class EntityEvent {
    private String methodName;
    private OperationType operationType;
    private BoundedContext boundedContext;
    private Object entity;
    private Object example;
    private Object primaryKey;
}
