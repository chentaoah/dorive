package com.gitee.spring.domain.coating.entity;

import com.gitee.spring.domain.core.entity.EntityPropertyLocation;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Field;

@Data
@AllArgsConstructor
public class PropertyDefinition {
    private Field field;
    private Class<?> fieldClass;
    private boolean collection;
    private Class<?> genericFieldClass;
    private String fieldName;
    private EntityPropertyLocation entityPropertyLocation;
}
