package com.gitee.spring.domain.core.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.core.annotation.AnnotationAttributes;

import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
public class EntityDefinition {
    private boolean root;
    private String accessPath;
    private AnnotatedElement annotatedElement;
    private Class<?> entityClass;
    private boolean collection;
    private Class<?> genericEntityClass;
    private String fieldName;
    private AnnotationAttributes attributes;
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
    private List<BindingDefinition> allBindingDefinitions;
    private List<BindingDefinition> boundBindingDefinitions;
    private List<BindingDefinition> contextBindingDefinitions;
    private BindingDefinition boundIdBindingDefinition;
    private Set<String> entityJoinAliases;
    private Set<String> fieldNames;
    private Map<String, EntityPropertyChain> entityPropertyChainMap;
}
