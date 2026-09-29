/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.binder.v1.impl.endpoint;

import com.gitee.dorive.base.v1.definition.entity.FieldDefinition;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FieldEndpoint extends SpELEndpoint {

    public FieldEndpoint(FieldDefinition fieldDefinition, String expression) {
        super(fieldDefinition, expression);
    }

}
