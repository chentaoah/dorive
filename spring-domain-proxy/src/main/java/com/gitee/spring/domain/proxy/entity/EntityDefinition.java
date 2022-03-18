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
    private AnnotationAttributes attributes;
    private EntityAssembler entityAssembler;
    private Object mapper;
}
