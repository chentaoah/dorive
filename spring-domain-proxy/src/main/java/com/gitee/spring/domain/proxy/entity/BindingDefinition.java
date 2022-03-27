package com.gitee.spring.domain.proxy.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.core.annotation.AnnotationAttributes;

@Data
@AllArgsConstructor
public class BindingDefinition {
    private AnnotationAttributes attributes;
    private boolean fromContext;
    private boolean bindId;
    private EntityPropertyChain boundEntityPropertyChain;
}
