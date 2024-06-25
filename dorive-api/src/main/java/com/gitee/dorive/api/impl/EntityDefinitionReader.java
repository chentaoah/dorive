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

package com.gitee.dorive.api.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.api.annotation.Binding;
import com.gitee.dorive.api.annotation.Entity;
import com.gitee.dorive.api.annotation.Order;
import com.gitee.dorive.api.entity.BindingDefinition;
import com.gitee.dorive.api.entity.EntityDefinition;
import com.gitee.dorive.api.entity.FieldDefinition;
import com.gitee.dorive.api.entity.FieldEntityDefinition;
import com.gitee.dorive.api.util.ReflectUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class EntityDefinitionReader {

    private static final Map<String, EntityDefinition> CACHE = new ConcurrentHashMap<>();
    private static final Set<String> LOCK = new ConcurrentHashSet<>();

    public EntityDefinition read(Class<?> type) {
        synchronized (CACHE) {
            String typeName = type.getName();
            EntityDefinition entityDefinition = CACHE.get(typeName);
            if (entityDefinition == null) {
                if (LOCK.add(typeName)) {
                    entityDefinition = doRead(type);
                    CACHE.put(typeName, entityDefinition);
                    LOCK.remove(typeName);
                } else {
                    log.info("The entity nested itself! type: " + type.getName());
                }
            }
            return entityDefinition;
        }
    }

    private EntityDefinition doRead(Class<?> type) {
        Entity entity = AnnotatedElementUtils.getMergedAnnotation(type, Entity.class);
        Assert.notNull(entity, "The @Entity does not exist!");
        assert entity != null;
        String name = entity.name();
        Class<?> source = entity.source();
        Class<?> factory = entity.factory();
        Class<?> repository = entity.repository();
        int priority = entity.priority();

        EntityDefinition entityDefinition = new EntityDefinition();
        entityDefinition.setName(StringUtils.isNotBlank(name) ? name : type.getSimpleName());
        entityDefinition.setSourceName(source.getName());
        entityDefinition.setFactoryName(factory.getName());
        entityDefinition.setRepositoryName(repository.getName());
        entityDefinition.setPriority(priority);
        entityDefinition.setGenericTypeName(type.getName());
        readFields(type, entityDefinition);
        return entityDefinition;
    }

    private void readFields(Class<?> type, EntityDefinition entityDefinition) {
        List<FieldDefinition> fieldDefinitions = new ArrayList<>();
        List<FieldEntityDefinition> fieldEntityDefinitions = new ArrayList<>();
        List<Field> fields = ReflectUtils.getAllFields(type);
        // 去重
        Map<String, Field> fieldMap = new LinkedHashMap<>();
        for (Field field : fields) {
            fieldMap.put(field.getName(), field);
        }
        for (Field field : fieldMap.values()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                // 所有字段
                FieldDefinition fieldDefinition = readField(field);
                if (fieldDefinition.isPrimary()) {
                    entityDefinition.setPrimaryKey(fieldDefinition.getFieldName());
                }
                fieldDefinitions.add(fieldDefinition);
                // 实体字段
                Entity entity = AnnotatedElementUtils.getMergedAnnotation(field, Entity.class);
                if (entity != null) {
                    FieldEntityDefinition fieldEntityDefinition = readFieldEntity(entity, field);
                    if (fieldEntityDefinition != null) {
                        fieldEntityDefinitions.add(fieldEntityDefinition);
                    }
                }
            }
        }
        entityDefinition.setFieldDefinitions(fieldDefinitions);
        entityDefinition.setFieldEntityDefinitions(fieldEntityDefinitions);
    }

    private FieldDefinition readField(Field field) {
        com.gitee.dorive.api.annotation.Field fieldAnnotation = AnnotatedElementUtils.getMergedAnnotation(field, com.gitee.dorive.api.annotation.Field.class);
        Class<?> type = field.getType();
        boolean collection = false;
        Class<?> genericType = field.getType();
        String fieldName = field.getName();
        if (Collection.class.isAssignableFrom(field.getType())) {
            collection = true;
            ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
            Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
            genericType = (Class<?>) actualTypeArgument;
        }

        FieldDefinition fieldDefinition = new FieldDefinition();
        if (fieldAnnotation != null) {
            fieldDefinition.setPrimary(fieldAnnotation.primary());
            fieldDefinition.setAlias(fieldAnnotation.alias());
            fieldDefinition.setValueObj(fieldAnnotation.valueObj());
            fieldDefinition.setMapExp(fieldAnnotation.mapExp());
            fieldDefinition.setConverterName(fieldAnnotation.converter().getName());
        } else {
            fieldDefinition.setPrimary(false);
            fieldDefinition.setAlias(StrUtil.toUnderlineCase(fieldName));
            fieldDefinition.setValueObj(false);
            fieldDefinition.setMapExp("");
            fieldDefinition.setConverterName(Object.class.getName());
        }
        if ("id".equals(fieldName)) {
            fieldDefinition.setPrimary(true);
        }
        if (StringUtils.isBlank(fieldDefinition.getAlias())) {
            fieldDefinition.setAlias(StrUtil.toUnderlineCase(fieldName));
        }
        fieldDefinition.setTypeName(type.getName());
        fieldDefinition.setCollection(collection);
        fieldDefinition.setGenericTypeName(genericType.getName());
        fieldDefinition.setFieldName(fieldName);
        return fieldDefinition;
    }

    private FieldEntityDefinition readFieldEntity(Entity entity, Field field) {
        boolean aggregate = entity.aggregate();
        Order order = AnnotatedElementUtils.getMergedAnnotation(field, Order.class);
        Class<?> type = field.getType();
        boolean collection = false;
        Class<?> genericType = field.getType();
        String fieldName = field.getName();
        if (Collection.class.isAssignableFrom(field.getType())) {
            collection = true;
            ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
            Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
            genericType = (Class<?>) actualTypeArgument;
        }

        EntityDefinition entityDefinition = read(genericType);
        if (entityDefinition == null) {
            return null;
        }
        FieldEntityDefinition fieldEntityDefinition = BeanUtil.copyProperties(entityDefinition, FieldEntityDefinition.class);

        fieldEntityDefinition.setAggregate(aggregate);
        fieldEntityDefinition.setBindingDefinitions(readBindingDefinitions(field));
        if (order != null) {
            fieldEntityDefinition.setSortBy(order.sortBy());
            fieldEntityDefinition.setOrder(order.order());
        }
        fieldEntityDefinition.setTypeName(type.getName());
        fieldEntityDefinition.setCollection(collection);
        fieldEntityDefinition.setFieldName(fieldName);

        // 可重写
        String name = entity.name();
        if (StringUtils.isNotBlank(name)) {
            fieldEntityDefinition.setName(name);
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
