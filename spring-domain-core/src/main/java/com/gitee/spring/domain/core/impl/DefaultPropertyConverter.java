package com.gitee.spring.domain.core.impl;

import com.gitee.spring.domain.core.api.PropertyConverter;
import com.gitee.spring.domain.core.entity.BindingDefinition;
import com.gitee.spring.domain.core.entity.BoundedContext;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DefaultPropertyConverter implements PropertyConverter {

    protected BindingDefinition bindingDefinition;

    @Override
    public Object convert(BoundedContext boundedContext, Object property) {
        return null;
    }

}
