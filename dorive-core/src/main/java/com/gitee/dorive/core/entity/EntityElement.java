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
package com.gitee.dorive.core.entity;

import cn.hutool.core.lang.Assert;
import com.gitee.dorive.core.annotation.Binding;
import com.gitee.dorive.core.api.PropertyProxy;
import com.gitee.dorive.core.impl.PropertyProxyFactory;
import com.gitee.dorive.core.util.ReflectUtils;
import com.gitee.dorive.core.annotation.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.Set;

@Data
@AllArgsConstructor
public class EntityElement {

    private Entity entityAnnotation;
    private Set<Binding> bindingAnnotations;
    private AnnotatedElement annotatedElement;
    private Class<?> entityClass;
    private boolean collection;
    private Class<?> genericEntityClass;
    private String fieldName;
    private Set<String> properties;
    private PropertyProxy primaryKeyProxy;

    public static EntityElement newEntityElement(AnnotatedElement annotatedElement) {
        Entity entityAnnotation = AnnotatedElementUtils.getMergedAnnotation(annotatedElement, Entity.class);
        Assert.notNull(entityAnnotation, "The annotation @Entity cannot be null!");
        Set<Binding> bindingAnnotations = AnnotatedElementUtils.getMergedRepeatableAnnotations(annotatedElement, Binding.class);

        if (annotatedElement instanceof Class) {
            Class<?> entityClass = (Class<?>) annotatedElement;
            return new EntityElement(
                    entityAnnotation,
                    bindingAnnotations,
                    annotatedElement,
                    entityClass,
                    false,
                    entityClass,
                    null,
                    ReflectUtils.getFieldNames(entityClass),
                    PropertyProxyFactory.newPropertyProxy(entityClass, "id"));

        } else if (annotatedElement instanceof Field) {
            Property property = new Property((Field) annotatedElement);
            return new EntityElement(
                    entityAnnotation,
                    bindingAnnotations,
                    annotatedElement,
                    property.getFieldClass(),
                    property.isCollection(),
                    property.getGenericFieldClass(),
                    property.getFieldName(),
                    ReflectUtils.getFieldNames(property.getGenericFieldClass()),
                    PropertyProxyFactory.newPropertyProxy(property.getGenericFieldClass(), "id"));

        } else {
            throw new RuntimeException("Unknown type!");
        }
    }

}
