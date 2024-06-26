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

import cn.hutool.core.util.ReflectUtil;
import com.gitee.dorive.api.def.EntityDef;
import com.gitee.dorive.api.def.FieldDef;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public class EntityField extends EntityEle {

    private Field field;
    private Class<?> type;
    private boolean collection;
    private Class<?> genericType;
    private String name;
    private FieldDef fieldDef;
    private EntityType entityType;

    public static boolean isComplexType(Class<?> type) {
        String className = type.getName();
        return !className.startsWith("java.lang.") && !className.startsWith("java.util.") && !type.isEnum();
    }

    public EntityField(Field field) {
        super(field);
        this.field = field;
        this.type = field.getType();
        this.collection = false;
        this.genericType = field.getType();
        this.name = field.getName();
        if (Collection.class.isAssignableFrom(field.getType())) {
            this.collection = true;
            ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
            Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
            this.genericType = (Class<?>) actualTypeArgument;
        }
        this.fieldDef = FieldDef.fromElement(field);
        resolveGenericType(this.genericType);
        initialize();
    }

    private void resolveGenericType(Class<?> genericType) {
        EntityDef entityDef = getEntityDef();
        if (entityDef != null) {
            EntityDef genericEntityDef = EntityDef.fromElement(genericType);
            if (genericEntityDef != null) {
                entityDef.mergeDef(genericEntityDef);
            }
        }
        if (isComplexType(genericType)) {
            entityType = EntityType.getInstance(genericType);
        }
    }

    @Override
    protected void doInitialize() {
        if (entityType != null) {
            entityType.initialize();
            setIdProxy(entityType.getIdProxy());
            setFieldAliasMapping(entityType.getFieldAliasMapping());
        }
    }

    @Override
    public Map<String, EntityField> getEntityFieldMap() {
        return entityType != null ? entityType.getEntityFieldMap() : null;
    }

    @Override
    public String getIdName() {
        return entityType != null ? entityType.getIdName() : null;
    }

    public boolean isSameType(EntityField entityField) {
        return type == entityField.getType() && genericType == entityField.getGenericType();
    }

    public Object getFieldValue(Object object) {
        return ReflectUtil.getFieldValue(object, field);
    }

}
