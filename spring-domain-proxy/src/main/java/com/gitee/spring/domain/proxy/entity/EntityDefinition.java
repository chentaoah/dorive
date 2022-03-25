package com.gitee.spring.domain.proxy.entity;

import com.gitee.spring.domain.proxy.api.EntityAssembler;
import lombok.Data;
import org.springframework.core.annotation.AnnotationAttributes;

@Data
public class EntityDefinition extends AnnotationAttributes {
    private EntityPropertyChain entityPropertyChain;
    private Class<?> genericEntityClass;
    private Object mapper;
    private Class<?> pojoClass;
    private EntityAssembler entityAssembler;

    public EntityDefinition(AnnotationAttributes other) {
        super(other);
    }
}
