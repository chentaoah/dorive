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
package com.gitee.dorive.core.entity.element;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReflectUtil;
import com.gitee.dorive.core.api.PropertyProxy;
import com.gitee.dorive.core.impl.PropertyProxyFactory;
import com.gitee.dorive.core.util.ReflectUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.Set;

@Data
@AllArgsConstructor
public class EntityElement {

    private AnnotatedElement element;
    private boolean collection;
    private Class<?> genericType;
    private Set<String> properties;
    private PropertyProxy primaryKeyProxy;

    public static EntityElement newEntityElement(AnnotatedElement annotatedElement) {
        if (annotatedElement instanceof Class) {
            Class<?> entityClass = (Class<?>) annotatedElement;
            Set<String> properties = ReflectUtils.getFieldNames(entityClass);
            PropertyProxy primaryKeyProxy = newPrimaryKeyProxy(entityClass);
            return new EntityElement(annotatedElement, false, entityClass, properties, primaryKeyProxy);

        } else if (annotatedElement instanceof Field) {
            Property property = new Property((Field) annotatedElement);
            Class<?> entityClass = property.getGenericFieldClass();
            Set<String> properties = ReflectUtils.getFieldNames(entityClass);
            PropertyProxy primaryKeyProxy = newPrimaryKeyProxy(entityClass);
            return new EntityElement(annotatedElement, property.isCollection(), entityClass, properties, primaryKeyProxy);
        }
        throw new RuntimeException("Unknown type!");
    }

    private static PropertyProxy newPrimaryKeyProxy(Class<?> entityClass) {
        Field field = ReflectUtil.getField(entityClass, "id");
        Assert.notNull(field, "The primary key not found! type: {}", entityClass.getName());
        return PropertyProxyFactory.newPropertyProxy(entityClass, "id");
    }

}
