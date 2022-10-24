package com.gitee.spring.domain.core3.api;

import com.gitee.spring.domain.core3.entity.definition.BindingDefinition;
import com.gitee.spring.domain.core3.entity.BoundedContext;

public interface Binder {

    BindingDefinition getBindingDefinition();

    Object getFieldValue(BoundedContext boundedContext, Object entity);

    void setFieldValue(BoundedContext boundedContext, Object entity, Object property);

    Object getBoundValue(BoundedContext boundedContext, Object rootEntity);

    void setBoundValue(BoundedContext boundedContext, Object rootEntity, Object property);

}
