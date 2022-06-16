package com.gitee.spring.domain.coating.entity;

import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.core.annotation.AnnotationAttributes;

import java.lang.reflect.Field;

@Data
@AllArgsConstructor
public class PropertyDefinition {
    private Field declaredField;
    private Class<?> fieldClass;
    private boolean collection;
    private Class<?> genericFieldClass;
    private String fieldName;
    private AnnotationAttributes attributes;
    private String locationAttribute;
    private String aliasAttribute;
    private String operatorAttribute;
    private boolean boundLocation;
    private EntityPropertyChain entityPropertyChain;
}
