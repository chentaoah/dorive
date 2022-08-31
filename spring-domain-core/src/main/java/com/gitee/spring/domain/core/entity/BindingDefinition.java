package com.gitee.spring.domain.core.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.core.annotation.AnnotationAttributes;

@Data
@AllArgsConstructor
public class BindingDefinition {
    private AnnotationAttributes attributes;
    private String fieldAttribute;
    private String aliasAttribute;
    private String bindAttribute;
    private String bindExpAttribute;
    private String bindAliasAttribute;
    private String propertyAttribute;
    private Class<?> converterClass;
}
