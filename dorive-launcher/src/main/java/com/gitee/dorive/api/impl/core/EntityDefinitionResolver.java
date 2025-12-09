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

package com.gitee.dorive.api.impl.core;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.api.annotation.core.Entity;
import com.gitee.dorive.api.annotation.core.Property;
import com.gitee.dorive.api.entity.core.EntityDefinition;
import com.gitee.dorive.api.entity.core.FieldDefinition;
import com.gitee.dorive.api.entity.core.FieldEntityDefinition;
import com.gitee.dorive.api.entity.core.PropertyDefinition;
import com.gitee.dorive.api.entity.core.def.*;
import com.gitee.dorive.api.impl.util.ReflectUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class EntityDefinitionResolver {

    private static final Map<String, EntityDefinition> CACHE = new ConcurrentHashMap<>();
    private static final Set<String> LOCK = new ConcurrentHashSet<>();

    public EntityDefinition resolve(Class<?> type) {
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
        EntityDef entityDef = EntityDef.fromElement(type);
        Assert.notNull(entityDef, "The @Entity does not exist!");

        if (entityDef != null) {
            String name = entityDef.getName();
            if (StringUtils.isBlank(name)) {
                entityDef.setName(type.getSimpleName());
            }
        }

        EntityDefinition entityDefinition = new EntityDefinition();
        entityDefinition.setEntityDef(entityDef);
        entityDefinition.setGenericType(type);

        // 解析字段
        readFields(type, entityDefinition);

        return entityDefinition;
    }

    private void readFields(Class<?> type, EntityDefinition entityDefinition) {
        List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
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
                // 上下文属性
                Property propertyAnnotation = AnnotatedElementUtils.getMergedAnnotation(field, Property.class);
                if (propertyAnnotation != null) {
                    PropertyDefinition propertyDefinition = readProperty(field);
                    propertyDefinitions.add(propertyDefinition);
                    continue;
                }
                // 所有字段
                FieldDefinition fieldDefinition = readField(field);
                if (fieldDefinition.isPrimary()) {
                    entityDefinition.setPrimaryKey(fieldDefinition.getFieldName());
                }
                fieldDefinitions.add(fieldDefinition);
                // 实体字段
                Entity entityAnnotation = AnnotatedElementUtils.getMergedAnnotation(field, Entity.class);
                if (entityAnnotation != null) {
                    FieldEntityDefinition fieldEntityDefinition = readFieldEntity(entityAnnotation, field);
                    if (fieldEntityDefinition != null) {
                        fieldEntityDefinitions.add(fieldEntityDefinition);
                    }
                }
            }
        }

        entityDefinition.setPropertyDefinitions(propertyDefinitions);
        entityDefinition.setFieldDefinitions(fieldDefinitions);
        entityDefinition.setFieldEntityDefinitions(fieldEntityDefinitions);
    }

    private PropertyDefinition readProperty(Field field) {
        PropertyDefinition propertyDefinition = new PropertyDefinition(field);
        propertyDefinition.setPropertyDef(PropertyDef.fromElement(field));
        return propertyDefinition;
    }

    private FieldDefinition readField(Field field) {
        FieldDefinition fieldDefinition = new FieldDefinition(field);
        String fieldName = fieldDefinition.getFieldName();

        FieldDef fieldDef = FieldDef.fromElement(field);
        if (fieldDef == null) {
            fieldDef = new FieldDef();
            fieldDef.setPrimary(false);
            fieldDef.setAlias(StrUtil.toUnderlineCase(fieldName));
            fieldDef.setValueObj(false);
            fieldDef.setExpression("");
            fieldDef.setConverter(Object.class);
        }
        if ("id".equals(fieldName)) {
            fieldDef.setPrimary(true);
        }
        if (StringUtils.isBlank(fieldDef.getAlias())) {
            fieldDef.setAlias(StrUtil.toUnderlineCase(fieldName));
        }
        fieldDefinition.setFieldDef(fieldDef);

        return fieldDefinition;
    }

    private FieldEntityDefinition readFieldEntity(Entity entityAnnotation, Field field) {
        com.gitee.dorive.api.entity.core.Field myField = new com.gitee.dorive.api.entity.core.Field(field);
        EntityDefinition entityDefinition = resolve(myField.getGenericType());
        if (entityDefinition == null) {
            return null;
        }
        FieldEntityDefinition fieldEntityDefinition = BeanUtil.copyProperties(entityDefinition, FieldEntityDefinition.class);
        fieldEntityDefinition.setField(myField);
        fieldEntityDefinition.setBindingDefs(BindingDef.fromElement(field));
        fieldEntityDefinition.setOrderByDef(OrderByDef.fromElement(field));

        EntityDef entityDef = fieldEntityDefinition.getEntityDef();
        EntityDef newEntityDef = BeanUtil.copyProperties(entityDef, EntityDef.class);
        // 可重写
        String name = entityAnnotation.name();
        if (StringUtils.isNotBlank(name)) {
            newEntityDef.setName(name);
        }
        newEntityDef.setAggregate(entityAnnotation.aggregate());
        newEntityDef.setRepository(entityAnnotation.repository());
        newEntityDef.setPriority(entityAnnotation.priority());
        fieldEntityDefinition.setEntityDef(newEntityDef);

        return fieldEntityDefinition;
    }

}
