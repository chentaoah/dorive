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

package com.gitee.dorive.api.entity.element;

import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.api.api.PropProxy;
import com.gitee.dorive.api.entity.def.FieldDef;
import com.gitee.dorive.api.exception.CircularDependencyException;
import com.gitee.dorive.api.impl.factory.PropProxyFactory;
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
    private Map<String, EntityField> entityFieldMap;

    public static synchronized EntityType getInstance(Class<?> type) {
        EntityType entityType = CACHE.get(type);
        if (entityType == null) {
            if (LOCK.add(type)) {
                entityType = new EntityType(type);
                CACHE.put(type, entityType);
                LOCK.remove(type);
            } else {
                throw new CircularDependencyException("Circular Dependency! type: " + type.getName());
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
                    entityFieldMap.put(entityField.getName(), entityField);

                } catch (CircularDependencyException e) {
                    log.warn(e.getMessage());
                }
            }
        }

        initialize();
    }

    @Override
    protected void doInitialize() {
        Class<?> genericType = getGenericType();
        int initialCapacity = entityFieldMap.size() * 4 / 3 + 1;
        PropProxy pkProxy = null;
        Map<String, String> propAliasMap = new LinkedHashMap<>(initialCapacity);

        for (EntityField entityField : entityFieldMap.values()) {
            String name = entityField.getName();
            FieldDef fieldDef = entityField.getFieldDef();
            if ("id".equals(name)) {
                pkProxy = PropProxyFactory.newPropProxy(genericType, "id");
            }
            String alias = fieldDef != null ? fieldDef.getAlias() : StrUtil.toUnderlineCase(name);
            propAliasMap.put(name, alias);
        }

        Assert.notNull(pkProxy, "The primary key not found! type: {}", genericType.getName());
        setPkProxy(pkProxy);
        setFieldAliasMapping(propAliasMap);
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

}
