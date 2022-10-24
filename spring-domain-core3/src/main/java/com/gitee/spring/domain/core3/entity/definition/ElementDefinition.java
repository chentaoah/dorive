package com.gitee.spring.domain.core3.entity.definition;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.AnnotatedElement;
import java.util.Set;

@Data
@AllArgsConstructor
public class ElementDefinition {
    private AnnotatedElement annotatedElement;
    private Class<?> entityClass;
    private boolean collection;
    private Class<?> genericEntityClass;
    private String fieldName;
    private Set<String> properties;
}
