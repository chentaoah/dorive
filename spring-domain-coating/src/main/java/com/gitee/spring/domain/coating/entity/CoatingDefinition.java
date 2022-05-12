package com.gitee.spring.domain.coating.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.core.annotation.AnnotationAttributes;

import java.util.Map;

@Data
@AllArgsConstructor
public class CoatingDefinition {
    private Class<?> entityClass;
    private Class<?> coatingClass;
    private AnnotationAttributes attributes;
    private String name;
    private Map<String, PropertyDefinition> propertyDefinitionMap;
}
