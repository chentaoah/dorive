package com.gitee.spring.domain.core.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.core.annotation.AnnotationAttributes;

import java.lang.reflect.AnnotatedElement;
import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
public class EntityDefinition {
    private boolean aggregateRoot;
    private String accessPath;
    private AnnotatedElement annotatedElement;
    private Class<?> entityClass;
    private boolean collection;
    private Class<?> genericEntityClass;
    private String fieldName;
    private AnnotationAttributes attributes;
    private String idAttribute;
    private Set<String> sceneAttribute;
    private Object mapper;
    private Class<?> pojoClass;
    private boolean sameType;
    private Class<?> mappedClass;
    private boolean useEntityExample;
    private boolean mapAsExample;
    private String orderByAsc;
    private String orderByDesc;
    private String[] orderBy;
    private String sort;
    private int orderAttribute;
    private Set<String> boundColumns;
    private boolean boundEntity;
    private Set<String> fieldNames;
    private Map<String, EntityPropertyChain> entityPropertyChainMap;
}
