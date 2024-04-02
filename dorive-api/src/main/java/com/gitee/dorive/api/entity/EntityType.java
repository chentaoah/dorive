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

package com.gitee.dorive.api.entity;

import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.api.api.PropProxy;
import com.gitee.dorive.api.def.FieldDef;
import com.gitee.dorive.api.exception.DefineEntityException;
import com.gitee.dorive.api.factory.PropProxyFactory;
import com.gitee.dorive.api.util.ReflectUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Slf4j
@EqualsAndHashCode(callSuper = false)
public class EntityType extends EntityEle {

    private static final Map<Class<?>, EntityType> CACHE = new ConcurrentHashMap<>();
    private static final Set<Class<?>> LOCK = new ConcurrentHashSet<>();

    private Class<?> type;
    private String name;
    private EntityField idField;
    private Map<String, EntityField> entityFieldMap;

    public static synchronized EntityType getInstance(Class<?> type) {
        EntityType entityType = CACHE.get(type);
        if (entityType == null) {
            if (LOCK.add(type)) {
                entityType = new EntityType(type);
                CACHE.put(type, entityType);
                LOCK.remove(type);
            } else {
                throw new DefineEntityException("The entity nested itself! type: " + type.getName());
            }
        }
        return entityType;
    }

    private EntityType(Class<?> type) {
        super(type);
        this.type = type;
        this.name = type.getName();
        List<Field> fields = ReflectUtils.getAllFields(type);
        this.entityFieldMap = new LinkedHashMap<>(fields.size() * 4 / 3 + 1);
        for (Field field : fields) {
            if (!Modifier.isStatic(field.getModifiers())) {
                try {
                    EntityField entityField = new EntityField(field);
                    String fieldName = entityField.getName();
                    if (idField == null) {
                        FieldDef fieldDef = entityField.getFieldDef();
                        if ("id".equals(fieldName) || (fieldDef != null && fieldDef.isId())) {
                            idField = entityField;
                        }
                    }
                    entityFieldMap.put(fieldName, entityField);

                } catch (DefineEntityException e) {
                    log.warn(e.getMessage());
                }
            }
        }
        initialize();
    }

    @Override
    protected void doInitialize() {
        Class<?> genericType = getGenericType();
        Assert.notNull(idField, "The id field cannot be null! type: {}", genericType.getName());
        PropProxy idProxy = PropProxyFactory.newPropProxy(genericType, idField.getName());
        setIdProxy(idProxy);

        Map<String, String> fieldAliasMapping = new LinkedHashMap<>(entityFieldMap.size() * 4 / 3 + 1);
        for (EntityField entityField : entityFieldMap.values()) {
            String fieldName = entityField.getName();
            FieldDef fieldDef = entityField.getFieldDef();
            String alias = fieldDef != null ? fieldDef.getAlias() : StrUtil.toUnderlineCase(fieldName);
            fieldAliasMapping.put(fieldName, alias);
        }
        setFieldAliasMapping(fieldAliasMapping);
    }

    @Override
    public boolean isCollection() {
        return false;
    }

    @Override
    public Class<?> getGenericType() {
        return type;
    }

    @Override
    public EntityType getEntityType() {
        return this;
    }

    @Override
    public Map<String, EntityField> getEntityFieldMap() {
        return entityFieldMap;
    }

    @Override
    public String getIdName() {
        return idField.getName();
    }

}
