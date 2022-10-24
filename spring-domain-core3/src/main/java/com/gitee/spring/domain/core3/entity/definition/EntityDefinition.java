package com.gitee.spring.domain.core3.entity.definition;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EntityDefinition {
    private String[] context;
    private Class<?> mapper;
    private String method;
    private String orderByAsc;
    private String orderByDesc;
    private String pageNum;
    private String pageSize;
    private int order;
    private Class<?> factory;
    private Class<?> repository;
}
