package com.gitee.spring.domain.coating.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CoatingDefinition {
    private Class<?> entityClass;
    private Class<?> coatingClass;
    private List<PropertyDefinition> propertyDefinitions;
    private List<PropertyDefinition> orderedPropertyDefinitions;
}
