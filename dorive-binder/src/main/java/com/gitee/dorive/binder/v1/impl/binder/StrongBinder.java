/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.binder.v1.impl.binder;

import com.gitee.dorive.base.v1.definition.def.BindingDef;
import com.gitee.dorive.base.v1.binder.api.Processor;
import com.gitee.dorive.binder.v1.impl.endpoint.BindEndpoint;
import com.gitee.dorive.binder.v1.impl.endpoint.FieldEndpoint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StrongBinder extends AbstractBinder {

    public StrongBinder(BindingDef bindingDef, FieldEndpoint fieldEndpoint, BindEndpoint bindEndpoint, Processor processor) {
        super(bindingDef, fieldEndpoint, bindEndpoint, processor);
    }

}
