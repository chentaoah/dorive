package com.gitee.spring.domain.proxy.entity;

import com.gitee.spring.domain.proxy.api.EntityAssembler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.core.annotation.AnnotationAttributes;

@Data
@Builder
@AllArgsConstructor
public class EntityDefinition {
    private EntityPropertyChain entityPropertyChain;
    private Class<?> genericEntityClass;
    private AnnotationAttributes attributes;
    private Object mapper;
    private Class<?> pojoClass;
    private EntityPropertyChain queryValueEntityPropertyChain;
    private EntityAssembler entityAssembler;
}
