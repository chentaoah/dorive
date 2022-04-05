package com.gitee.spring.domain.proxy.entity;

import com.gitee.spring.domain.proxy.api.EntityAssembler;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.core.annotation.AnnotationAttributes;

import java.util.List;

@Data
@AllArgsConstructor
public class EntityDefinition {
    private boolean root;
    private String accessPath;
    private EntityPropertyChain entityPropertyChain;
    private Class<?> entityClass;
    private boolean collection;
    private Class<?> genericEntityClass;
    private AnnotationAttributes attributes;
    private Object mapper;
    private Class<?> pojoClass;
    private EntityAssembler entityAssembler;
    private List<BindingDefinition> bindingDefinitions;
    private BindingDefinition boundIdBindingDefinition;
}
