/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gitee.dorive.api.entity.core;

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
