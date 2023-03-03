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
import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.core.annotation.Alias;
import com.gitee.dorive.api.api.PropProxy;
import com.gitee.dorive.core.api.constant.Order;
import com.gitee.dorive.core.entity.definition.EntityDefinition;
import com.gitee.dorive.core.entity.executor.OrderBy;
import com.gitee.dorive.api.impl.factory.PropProxyFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class EntityElement {

    private AnnotatedElement annotatedElement;
    private boolean collection;
    private Class<?> genericType;
    private PropProxy primaryKeyProxy;
    private Map<String, PropertyDef> propertyDefMap;

    public static EntityElement newEntityElement(AnnotatedElement annotatedElement) {
        boolean isCollection = false;
        Class<?> entityClass = null;

        if (annotatedElement instanceof Class) {
            entityClass = (Class<?>) annotatedElement;

        } else if (annotatedElement instanceof Field) {
            Property property = new Property((Field) annotatedElement);
            isCollection = property.isCollection();
            entityClass = property.getGenericFieldClass();
        }

        EntityElement entityElement = new EntityElement(annotatedElement, isCollection, entityClass, null, null);
        return processEntityElement(entityElement);
    }

    private static EntityElement processEntityElement(EntityElement entityElement) {
        Class<?> genericType = entityElement.getGenericType();
        PropProxy primaryKeyProxy = newPrimaryKeyProxy(genericType);

        Field[] fields = ReflectUtil.getFields(genericType);
        Map<String, PropertyDef> propertyDefMap = new LinkedHashMap<>(fields.length * 4 / 3 + 1);
        for (Field field : fields) {
            String property = field.getName();
            boolean isAssign = false;
            String alias = StrUtil.toUnderlineCase(property);
            Alias aliasAnnotation = field.getAnnotation(Alias.class);
            if (aliasAnnotation != null) {
                isAssign = true;
                alias = aliasAnnotation.value();
            }
            propertyDefMap.put(property, new PropertyDef(property, isAssign, alias));
        }

        entityElement.setPrimaryKeyProxy(primaryKeyProxy);
        entityElement.setPropertyDefMap(propertyDefMap);
        return entityElement;
    }

    private static PropProxy newPrimaryKeyProxy(Class<?> entityClass) {
        Field field = ReflectUtil.getField(entityClass, "id");
        Assert.notNull(field, "The primary key not found! type: {}", entityClass.getName());
        return PropProxyFactory.newPropProxy(entityClass, "id");
    }

    public String toAlias(String property) {
        PropertyDef propertyDef = propertyDefMap.get(property);
        return propertyDef != null ? propertyDef.getAlias() : StrUtil.toUnderlineCase(property);
    }

    public List<String> toAliases(List<String> properties) {
        if (properties != null && !properties.isEmpty()) {
            List<String> columns = new ArrayList<>(properties.size());
            for (String property : properties) {
                String alias = toAlias(property);
                columns.add(alias);
            }
            return columns;
        }
        return properties;
    }

    public OrderBy newDefaultOrderBy(EntityDefinition entityDefinition) {
        if (StringUtils.isNotBlank(entityDefinition.getOrderByAsc())) {
            List<String> properties = StrUtil.splitTrim(entityDefinition.getOrderByAsc(), ",");
            List<String> columns = toAliases(properties);
            return new OrderBy(columns, Order.ASC);
        }
        if (StringUtils.isNotBlank(entityDefinition.getOrderByDesc())) {
            List<String> properties = StrUtil.splitTrim(entityDefinition.getOrderByDesc(), ",");
            List<String> columns = toAliases(properties);
            return new OrderBy(columns, Order.DESC);
        }
        return null;
    }

    public Map<String, String> newAliasPropMapping() {
        Map<String, String> mapping = new LinkedHashMap<>();
        for (PropertyDef propertyDef : propertyDefMap.values()) {
            mapping.put(propertyDef.getAlias(), propertyDef.getProperty());
        }
        return mapping;
    }

    @Data
    @AllArgsConstructor
    public static class PropertyDef {
        private String property;
        private boolean assign;
        private String alias;
    }

}
