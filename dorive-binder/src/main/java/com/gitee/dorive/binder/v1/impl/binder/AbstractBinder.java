/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.binder.v1.impl.binder;

import cn.hutool.core.lang.Assert;
import com.gitee.dorive.base.v1.definition.def.BindingDef;
import com.gitee.dorive.base.v1.binder.api.Binder;
import com.gitee.dorive.base.v1.binder.api.Processor;
import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.binder.v1.impl.endpoint.BindEndpoint;
import com.gitee.dorive.binder.v1.impl.endpoint.FieldEndpoint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public abstract class AbstractBinder implements Binder {
    protected BindingDef bindingDef;
    protected FieldEndpoint fieldEndpoint;
    protected BindEndpoint bindEndpoint;
    protected Processor processor;

    @Override
    public String getSourceField() {
        String field = bindingDef.getSource();
        Assert.notBlank(field, "The field cannot be blank!");
        return field;
    }

    @Override
    public String getTargetField() {
        String targetField = bindingDef.getTargetField();
        Assert.notBlank(targetField, "The targetField cannot be blank!");
        return targetField;
    }

    @Override
    public Object getSourceFieldValue(Context context, Object entity) {
        return fieldEndpoint.getValue(entity);
    }

    @Override
    public void setSourceFieldValue(Context context, Object entity, Object value) {
        fieldEndpoint.setValue(entity, value);
    }

    @Override
    public Object getTargetFieldValue(Context context, Object entity) {
        return bindEndpoint.getValue(entity);
    }

    @Override
    public void setTargetFieldValue(Context context, Object entity, Object value) {
        bindEndpoint.setValue(entity, value);
    }

    @Override
    public Object input(Context context, Object value) {
        return value == null || processor == null ? value : processor.input(context, value);
    }

    @Override
    public Object output(Context context, Object value) {
        return value == null || processor == null ? value : processor.output(context, value);
    }

    public boolean isBindCollection() {
        return bindEndpoint.getFieldDefinition().isCollection();
    }

    public String getBelongAccessPath() {
        return bindEndpoint.getBelongAccessPath();
    }

    public boolean isSameType() {
        return fieldEndpoint.getFieldDefinition().isSameType(bindEndpoint.getFieldDefinition());
    }
}
