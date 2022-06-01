package com.gitee.spring.domain.core.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.core.annotation.AnnotationAttributes;

import java.util.List;

@Data
@AllArgsConstructor
public class EntityDefinition {
    private boolean root;
    private String accessPath;
    private Class<?> entityClass;
    private boolean collection;
    private Class<?> genericEntityClass;
    private String fieldName;
    private AnnotationAttributes attributes;
    private String[] sceneAttribute;
    private Object mapper;
    private Class<?> pojoClass;
    private boolean sameType;
    private boolean useEntityExample;
    private boolean mapAsExample;
    private String orderByAsc;
    private String orderByDesc;
    private String orderBy;
    private String sort;
    private int orderAttribute;
    private List<BindingDefinition> bindingDefinitions;
    private BindingDefinition boundIdBindingDefinition;
}
