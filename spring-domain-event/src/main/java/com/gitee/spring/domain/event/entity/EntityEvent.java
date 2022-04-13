package com.gitee.spring.domain.event.entity;

import com.gitee.spring.domain.core.entity.BoundedContext;
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
