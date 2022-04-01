package com.gitee.spring.domain.proxy.entity;

import com.gitee.spring.domain.proxy.api.EntityAssembler;
import com.gitee.spring.domain.proxy.api.EntitySelector;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.core.annotation.AnnotationAttributes;

import java.util.List;

@Data
@AllArgsConstructor
public class EntityDefinition {
    private String accessPath;
    private boolean root;
    private EntityPropertyChain entityPropertyChain;
    private Class<?> entityClass;
    private boolean collection;
    private Class<?> genericEntityClass;
    private AnnotationAttributes attributes;
    private Object mapper;
    private Class<?> pojoClass;
    private EntitySelector entitySelector;
    private EntityAssembler entityAssembler;
    private List<BindingDefinition> bindingDefinitions;
    private BindingDefinition boundIdBindingDefinition;
}
