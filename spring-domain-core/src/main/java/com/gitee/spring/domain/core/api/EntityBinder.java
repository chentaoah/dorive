package com.gitee.spring.domain.core.api;

import com.gitee.spring.domain.core.entity.BindingDefinition;
import com.gitee.spring.domain.core.entity.BoundedContext;

public interface EntityBinder {

    BindingDefinition getBindingDefinition();

    String getColumnName();

    Object getBoundValue(BoundedContext boundedContext, Object rootEntity);

    void setBoundValue(BoundedContext boundedContext, Object rootEntity, Object property);

    Object getFieldValue(BoundedContext boundedContext, Object entity);

    void setFieldValue(BoundedContext boundedContext, Object entity, Object property);

}
