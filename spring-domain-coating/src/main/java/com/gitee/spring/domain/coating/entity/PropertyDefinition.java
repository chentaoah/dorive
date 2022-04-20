package com.gitee.spring.domain.coating.entity;

import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import com.gitee.spring.domain.core.repository.ConfiguredRepository;
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
    private EntityPropertyChain entityPropertyChain;
    private ConfiguredRepository belongConfiguredRepository;
}
