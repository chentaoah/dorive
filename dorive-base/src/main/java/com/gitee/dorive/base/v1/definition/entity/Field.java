/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.definition.entity;

import cn.hutool.core.util.ReflectUtil;
import lombok.Data;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

@Data
public class Field {
    private java.lang.reflect.Field field;
    private Class<?> type;
    private boolean collection;
    private Class<?> genericType;
    private String fieldName;

    public Field(java.lang.reflect.Field field) {
        this.field = field;
        this.type = field.getType();
        this.collection = false;
        this.genericType = field.getType();
        this.fieldName = field.getName();
        if (Collection.class.isAssignableFrom(field.getType())) {
            this.collection = true;
            ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
            Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
            this.genericType = (Class<?>) actualTypeArgument;
        }
    }

    public boolean isSameType(Field field) {
        return type == field.getType() && genericType == field.getGenericType();
    }

    public Object getFieldValue(Object object) {
        return ReflectUtil.getFieldValue(object, field);
    }
}
