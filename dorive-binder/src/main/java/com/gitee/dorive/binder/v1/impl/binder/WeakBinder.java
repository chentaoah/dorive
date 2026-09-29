/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.binder.v1.impl.binder;

import com.gitee.dorive.base.v1.definition.def.BindingDef;
import com.gitee.dorive.base.v1.binder.api.Processor;
import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.binder.v1.impl.endpoint.BindEndpoint;
import com.gitee.dorive.binder.v1.impl.endpoint.FieldEndpoint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeakBinder extends AbstractBinder {

    public WeakBinder(BindingDef bindingDef, FieldEndpoint fieldEndpoint, BindEndpoint bindEndpoint, Processor processor) {
        super(bindingDef, fieldEndpoint, bindEndpoint, processor);
    }

    @Override
    public Object input(Context context, Object value) {
        return processor.input(context, value);
    }

    @Override
    public Object output(Context context, Object value) {
        return processor.output(context, value);
    }

}
