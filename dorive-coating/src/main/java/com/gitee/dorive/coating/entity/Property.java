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
package com.gitee.dorive.coating.entity;

import cn.hutool.core.util.ReflectUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

@Data
@NoArgsConstructor
public class Property {

    private Field field;
    private Class<?> type;
    private boolean collection;
    private Class<?> genericType;
    private String name;

    public Property(Field Field) {
        Class<?> fieldClass = Field.getType();
        boolean isCollection = false;
        Class<?> fieldGenericClass = fieldClass;
        String fieldName = Field.getName();

        if (Collection.class.isAssignableFrom(fieldClass)) {
            isCollection = true;
            ParameterizedType parameterizedType = (ParameterizedType) Field.getGenericType();
            Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
            fieldGenericClass = (Class<?>) actualTypeArgument;
        }

        this.field = Field;
        this.type = fieldClass;
        this.collection = isCollection;
        this.genericType = fieldGenericClass;
        this.name = fieldName;
    }

    public boolean isSameType(Property property) {
        return type == property.getType() && genericType == property.getGenericType();
    }

    public Object getFieldValue(Object object) {
        return ReflectUtil.getFieldValue(object, field);
    }

}
