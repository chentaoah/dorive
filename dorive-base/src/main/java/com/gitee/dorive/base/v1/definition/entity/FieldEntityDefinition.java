/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.definition.entity;

import com.gitee.dorive.base.v1.definition.def.BindingDef;
import com.gitee.dorive.base.v1.definition.def.OrderByDef;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class FieldEntityDefinition extends EntityDefinition {
    private Field field;
    private List<BindingDef> bindingDefs;
    private OrderByDef orderByDef;

    public java.lang.reflect.Field getJavaField() {
        return field != null ? field.getField() : null;
    }

    public boolean isCollection() {
        return field != null && field.isCollection();
    }

    public String getFieldName() {
        return field != null ? field.getFieldName() : null;
    }
}
