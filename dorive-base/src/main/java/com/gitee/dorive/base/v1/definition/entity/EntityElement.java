/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.definition.entity;

import cn.hutool.core.util.ReflectUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EntityElement extends FieldEntityDefinition {
    private String accessPath;
    private Map<String, String> fieldAliasMap;

    public boolean isRoot() {
        return "/".equals(accessPath);
    }

    public Object getValue(Object entity) {
        return ReflectUtil.getFieldValue(entity, getFieldName());
    }

    public void setValue(Object entity, Object value) {
        ReflectUtil.setFieldValue(entity, getFieldName(), value);
    }

    public Object getPrimaryKey(Object entity) {
        return ReflectUtil.getFieldValue(entity, getPrimaryKey());
    }

    public void setPrimaryKey(Object entity, Object value) {
        ReflectUtil.setFieldValue(entity, getPrimaryKey(), value);
    }
}
