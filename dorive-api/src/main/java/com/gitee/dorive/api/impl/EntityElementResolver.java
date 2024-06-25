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

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.api.entity.def.BindingDef;
import com.gitee.dorive.api.entity.def.EntityDef;
import com.gitee.dorive.api.entity.def.FieldDef;
import com.gitee.dorive.api.entity.def.OrderDef;
import com.gitee.dorive.api.entity.ele.EntityElement;
import com.gitee.dorive.api.entity.ele.FieldElement;
import com.gitee.dorive.api.entity.BindingDefinition;
import com.gitee.dorive.api.entity.EntityDefinition;
import com.gitee.dorive.api.entity.FieldDefinition;
import com.gitee.dorive.api.entity.FieldEntityDefinition;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class EntityElementResolver {

    public List<EntityElement> resolve(EntityDefinition entityDefinition) {
        List<EntityElement> entityElements = new ArrayList<>();

        String accessPath = "/";
        EntityElement entityElement = resolveElement(accessPath, entityDefinition);
        entityElements.add(entityElement);

        List<FieldEntityDefinition> fieldEntityDefinitions = entityDefinition.getFieldEntityDefinitions();
        for (FieldEntityDefinition fieldEntityDefinition : fieldEntityDefinitions) {
            String fieldAccessPath = "/" + fieldEntityDefinition.getFieldName();
            EntityElement fieldEntityElement = resolveElement(fieldAccessPath, fieldEntityDefinition);
            entityElements.add(fieldEntityElement);
        }

        return entityElements;
    }

    private EntityElement resolveElement(String accessPath, EntityDefinition entityDefinition) {
        Class<?> genericType = ClassUtil.loadClass(entityDefinition.getGenericTypeName());

        EntityDef entityDef = new EntityDef();
        entityDef.setName(entityDefinition.getName());
        entityDef.setSource(ClassUtil.loadClass(entityDefinition.getSourceName()));
        entityDef.setFactory(ClassUtil.loadClass(entityDefinition.getFactoryName()));
        entityDef.setRepository(ClassUtil.loadClass(entityDefinition.getRepositoryName()));
        entityDef.setPriority(entityDefinition.getPriority());
        entityDef.setAggregate(entityDefinition.isAggregate());

        List<BindingDefinition> bindingDefinitions = entityDefinition.getBindingDefinitions();
        List<BindingDef> bindingDefs = new ArrayList<>(bindingDefinitions.size());
        for (BindingDefinition bindingDefinition : bindingDefinitions) {
            BindingDef bindingDef = new BindingDef();
            bindingDef.setField(bindingDefinition.getField());
            bindingDef.setValue(bindingDefinition.getValue());
            bindingDef.setBindExp(bindingDefinition.getBindExp());
            bindingDef.setProcessExp(bindingDefinition.getProcessExp());
            bindingDef.setProcessor(ClassUtil.loadClass(bindingDefinition.getProcessorName()));
            bindingDef.setBindField(bindingDefinition.getBindField());
            bindingDefs.add(bindingDef);
        }

        OrderDef orderDef = new OrderDef();
        orderDef.setSortBy(entityDefinition.getSortBy());
        orderDef.setOrder(entityDefinition.getOrder());

        List<FieldDefinition> fieldDefinitions = entityDefinition.getFieldDefinitions();
        List<FieldElement> fieldElements = new ArrayList<>(fieldDefinitions.size());
        for (FieldDefinition fieldDefinition : fieldDefinitions) {
            FieldElement fieldElement = new FieldElement();
            fieldElement.setFieldDefinition(fieldDefinition);

            FieldDef fieldDef = new FieldDef();
            fieldDef.setPrimary(fieldDefinition.isPrimary());
            fieldDef.setAlias(fieldDefinition.getAlias());
            fieldDef.setValueObj(fieldDefinition.isValueObj());
            fieldDef.setMapExp(fieldDefinition.getMapExp());
            fieldDef.setConverter(ClassUtil.loadClass(fieldDefinition.getConverterName()));
            fieldElement.setFieldDef(fieldDef);

            fieldElement.setGenericType(ClassUtil.loadClass(fieldDefinition.getGenericTypeName()));
            fieldElements.add(fieldElement);
        }

        Map<String, String> fieldAliasMapping = new LinkedHashMap<>(fieldDefinitions.size() * 4 / 3 + 1);
        for (FieldDefinition fieldDefinition : fieldDefinitions) {
            String alias = fieldDefinition.getAlias();
            String fieldName = fieldDefinition.getFieldName();
            if (StringUtils.isBlank(alias)) {
                alias = StrUtil.toUnderlineCase(fieldName);
            }
            fieldAliasMapping.put(fieldName, alias);
        }

        return new EntityElement(entityDefinition, accessPath, entityDef, bindingDefs, orderDef, genericType, fieldElements, fieldAliasMapping);
    }

}
