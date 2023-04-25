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
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.api.api.PropProxy;
import com.gitee.dorive.api.entity.def.AliasDef;
import com.gitee.dorive.api.impl.factory.PropProxyFactory;
import com.gitee.dorive.api.util.ReflectUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Data
@EqualsAndHashCode(callSuper = true)
public class EntityType extends EntityEle {

    private static final Map<Class<?>, EntityType> CACHE = new ConcurrentHashMap<>();
    private static final Set<Class<?>> LOCK = new ConcurrentHashSet<>();

    private Class<?> type;
    private String name;
    private Map<String, EntityField> entityFields = new LinkedHashMap<>();

    public static synchronized EntityType getInstance(Class<?> type) {
        EntityType entityType = CACHE.get(type);
        if (entityType == null) {
            if (LOCK.add(type)) {
                entityType = new EntityType(type);
                LOCK.remove(type);
                CACHE.put(type, entityType);
            }
        }
        return entityType;
    }

    private EntityType(Class<?> type) {
        super(type);
        this.type = type;
        this.name = type.getName();
        for (Field field : ReflectUtils.getAllFields(type)) {
            if (!Modifier.isStatic(field.getModifiers())) {
                EntityField entityField = new EntityField(field);
                entityFields.put(entityField.getName(), entityField);
            }
        }
        initialize();
    }

    @Override
    protected void doInitialize() {
        Class<?> genericType = getGenericType();
        boolean hasField = ReflectUtil.hasField(genericType, "id");
        Assert.isTrue(hasField, "The primary key not found! type: {}", genericType.getName());
        PropProxy pkProxy = PropProxyFactory.newPropProxy(genericType, "id");
        setPkProxy(pkProxy);

        Map<String, String> aliasMap = new LinkedHashMap<>();
        for (EntityField entityField : entityFields.values()) {
            String name = entityField.getName();
            AliasDef aliasDef = entityField.getAliasDef();
            String alias = aliasDef != null ? aliasDef.getValue() : StrUtil.toUnderlineCase(name);
            aliasMap.put(name, alias);
        }
        setAliasMap(aliasMap);
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

    public void check() {
        Assert.isTrue(isAnnotatedEntity(), "No @Entity annotation found! type: {}", name);
    }

}
