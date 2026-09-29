/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.definition.entity;

import com.gitee.dorive.base.v1.definition.def.FieldDef;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class FieldDefinition extends Field {
    private FieldDef fieldDef;

    public FieldDefinition(java.lang.reflect.Field field) {
        super(field);
    }

    public boolean isPrimary() {
        return fieldDef.isPrimary();
    }

    public String getAlias() {
        return fieldDef.getAlias();
    }
}
