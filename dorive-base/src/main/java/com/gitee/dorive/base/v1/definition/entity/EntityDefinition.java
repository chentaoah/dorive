/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.definition.entity;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReflectUtil;
import com.gitee.dorive.base.v1.definition.def.EntityDef;
import lombok.Data;

import java.util.List;

@Data
public class EntityDefinition {
    private EntityDef entityDef;
    private Class<?> genericType;
    private String primaryKey;
    private List<FieldDefinition> fieldDefinitions;
    private List<FieldEntityDefinition> fieldEntityDefinitions;

    public boolean hasField(String fieldName) {
        return ReflectUtil.hasField(genericType, fieldName);
    }

    public FieldDefinition getFieldDefinition(String fieldName) {
        return CollUtil.findOne(fieldDefinitions, fieldDefinition -> fieldName.equals(fieldDefinition.getFieldName()));
    }
}
