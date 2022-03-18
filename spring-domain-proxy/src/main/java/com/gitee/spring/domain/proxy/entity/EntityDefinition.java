package com.gitee.spring.domain.proxy.entity;

import com.gitee.spring.domain.proxy.api.EntityAccessor;
import com.gitee.spring.domain.proxy.api.EntityAssembler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.core.annotation.AnnotationAttributes;

@Data
@Builder
@AllArgsConstructor
public class EntityDefinition {
    private Class<?> entityClass;
    private String accessPath;
    private AnnotationAttributes attributes;
    private EntityAccessor entityAccessor;
    private EntityAssembler entityAssembler;
    private Object mapper;
}
