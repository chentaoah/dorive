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

package com.gitee.dorive.def.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.api.annotation.Binding;
import com.gitee.dorive.api.annotation.Entity;
import com.gitee.dorive.api.annotation.Order;
import com.gitee.dorive.api.util.ReflectUtils;
import com.gitee.dorive.def.entity.BindingDefinition;
import com.gitee.dorive.def.entity.EntityDefinition;
import com.gitee.dorive.def.entity.FieldDefinition;
import com.gitee.dorive.def.entity.FieldEntityDefinition;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class EntityDefinitionReader {

    private static final Map<String, EntityDefinition> entityDefinitionMap = new ConcurrentHashMap<>();

    public EntityDefinition read(Class<?> type) {
        String typeName = type.getName();
        EntityDefinition entityDefinition = entityDefinitionMap.get(typeName);
        if (entityDefinition != null) {
            return entityDefinition;
        }

        Entity entity = AnnotationUtils.getAnnotation(type, Entity.class);
        Assert.notNull(entity, "The @Entity does not exist!");
        assert entity != null;
        String name = entity.name();
        Class<?> source = entity.source();
        Class<?> factory = entity.factory();
        Class<?> repository = entity.repository();

        entityDefinition = new EntityDefinition();
        entityDefinition.setName(StringUtils.isNotBlank(name) ? name : type.getSimpleName());
        entityDefinition.setSourceName(source.getName());
        entityDefinition.setFactoryName(factory.getName());
        entityDefinition.setRepositoryName(repository.getName());
        entityDefinition.setPriority(0);
        entityDefinition.setClassName(typeName);
        readFields(type, entityDefinition);
        entityDefinitionMap.put(typeName, entityDefinition);

        return entityDefinition;
    }

    private void readFields(Class<?> type, EntityDefinition entityDefinition) {
        List<FieldDefinition> fieldDefinitions = new ArrayList<>();
        List<FieldEntityDefinition> fieldEntityDefinitions = new ArrayList<>();
        List<Field> fields = ReflectUtils.getAllFields(type);
        for (Field field : fields) {
            if (!Modifier.isStatic(field.getModifiers())) {
                Entity entity = AnnotationUtils.getAnnotation(field, Entity.class);
                if (entity == null) {
                    FieldDefinition fieldDefinition = readField(field);
                    if (fieldDefinition.isPrimary()) {
                        entityDefinition.setPrimaryKey(fieldDefinition.getName());
                    }
                    fieldDefinitions.add(fieldDefinition);
                } else {
                    fieldEntityDefinitions.add(readFieldEntity(entity, field));
                }
            }
        }
        entityDefinition.setFieldDefinitions(fieldDefinitions);
        entityDefinition.setFieldEntityDefinitions(fieldEntityDefinitions);
    }

    private FieldDefinition readField(Field field) {
        com.gitee.dorive.api.annotation.Field fieldAnnotation = AnnotationUtils.getAnnotation(field, com.gitee.dorive.api.annotation.Field.class);
        Class<?> type = field.getType();
        boolean collection = false;
        Class<?> genericType = field.getType();
        String name = field.getName();
        if (Collection.class.isAssignableFrom(field.getType())) {
            collection = true;
            ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
            Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
            genericType = (Class<?>) actualTypeArgument;
        }

        FieldDefinition fieldDefinition = new FieldDefinition();
        if (fieldAnnotation != null) {
            fieldDefinition.setPrimary(fieldAnnotation.id());
            fieldDefinition.setAlias(fieldAnnotation.alias());
            fieldDefinition.setValueObj(fieldAnnotation.valueObj());
            fieldDefinition.setMapExp(fieldAnnotation.mapExp());
            fieldDefinition.setConverterName(fieldAnnotation.converter().getName());
        } else {
            fieldDefinition.setPrimary(false);
            fieldDefinition.setAlias(StrUtil.toUnderlineCase(name));
            fieldDefinition.setValueObj(false);
            fieldDefinition.setMapExp("");
            fieldDefinition.setConverterName(Object.class.getName());
        }
        if ("id".equals(name)) {
            fieldDefinition.setPrimary(true);
        }
        fieldDefinition.setType(type.getName());
        fieldDefinition.setCollection(collection);
        fieldDefinition.setGenericType(genericType.getName());
        fieldDefinition.setName(name);
        return fieldDefinition;
    }

    private FieldEntityDefinition readFieldEntity(Entity entity, Field field) {
        boolean aggregate = entity.aggregate();
        Order order = AnnotationUtils.getAnnotation(field, Order.class);
        Class<?> type = field.getType();
        boolean collection = false;
        Class<?> genericType = field.getType();
        String name = field.getName();
        if (Collection.class.isAssignableFrom(field.getType())) {
            collection = true;
            ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
            Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
            genericType = (Class<?>) actualTypeArgument;
        }

        EntityDefinition entityDefinition = read(genericType);
        FieldEntityDefinition fieldEntityDefinition = BeanUtil.copyProperties(entityDefinition, FieldEntityDefinition.class);

        fieldEntityDefinition.setAggregate(aggregate);
        fieldEntityDefinition.setBindingDefinitions(readBindingDefinitions(field));
        if (order != null) {
            fieldEntityDefinition.setPriority(order.priority());
            fieldEntityDefinition.setSortBy(order.sortBy());
            fieldEntityDefinition.setOrder(order.order());
        }
        fieldEntityDefinition.setType(type.getName());
        fieldEntityDefinition.setCollection(collection);
        fieldEntityDefinition.setName(name);

        // 可重写
        String entityName = entity.name();
        if (StringUtils.isNotBlank(entityName)) {
            fieldEntityDefinition.setName(entityName);
        }
        Class<?> source = entity.source();
        if (source != Object.class) {
            fieldEntityDefinition.setSourceName(source.getName());
        }
        Class<?> factory = entity.factory();
        if (factory != Object.class) {
            fieldEntityDefinition.setFactoryName(factory.getName());
        }

        return fieldEntityDefinition;
    }

    private List<BindingDefinition> readBindingDefinitions(Field field) {
        Set<Binding> bindings = AnnotatedElementUtils.getMergedRepeatableAnnotations(field, Binding.class);
        List<BindingDefinition> bindingDefinitions = new ArrayList<>(bindings.size());
        for (Binding binding : bindings) {
            BindingDefinition bindingDefinition = new BindingDefinition();
            bindingDefinition.setField(binding.field());
            bindingDefinition.setValue(binding.value());
            bindingDefinition.setBindExp(binding.bindExp());
            bindingDefinition.setProcessExp(binding.processExp());
            bindingDefinition.setProcessorName(binding.processor().getName());
            bindingDefinition.setBindField(binding.bindField());
            bindingDefinitions.add(bindingDefinition);
        }
        return bindingDefinitions;
    }

}
