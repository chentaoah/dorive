/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.binder.v1.impl.binder;

import cn.hutool.core.convert.Convert;
import com.gitee.dorive.base.v1.definition.def.BindingDef;
import com.gitee.dorive.base.v1.binder.api.Processor;
import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.binder.v1.impl.endpoint.BindEndpoint;
import com.gitee.dorive.binder.v1.impl.endpoint.FieldEndpoint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValueFilterBinder extends AbstractBinder {

    private Object value;

    public ValueFilterBinder(BindingDef bindingDef, FieldEndpoint fieldEndpoint, BindEndpoint bindEndpoint, Processor processor) {
        super(bindingDef, fieldEndpoint, bindEndpoint, processor);
        Class<?> genericType = fieldEndpoint.getFieldDefinition().getGenericType();
        this.value = Convert.convert(genericType, bindingDef.getLiteral());
    }

    @Override
    public Object getTargetFieldValue(Context context, Object entity) {
        return value;
    }

    @Override
    public void setTargetFieldValue(Context context, Object entity, Object property) {
        throw new UnsupportedOperationException();
    }

}
