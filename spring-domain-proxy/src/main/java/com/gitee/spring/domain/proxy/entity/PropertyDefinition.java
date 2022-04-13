package com.gitee.spring.domain.proxy.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PropertyDefinition {
    private Class<?> fieldClass;
    private boolean collection;
    private Class<?> genericFieldClass;
    private String fieldName;
}
