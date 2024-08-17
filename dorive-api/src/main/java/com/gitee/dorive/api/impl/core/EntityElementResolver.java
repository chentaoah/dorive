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

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.api.entity.core.def.BindingDef;
import com.gitee.dorive.api.entity.core.def.EntityDef;
import com.gitee.dorive.api.entity.core.def.FieldDef;
import com.gitee.dorive.api.entity.core.def.OrderDef;
import com.gitee.dorive.api.entity.core.ele.EntityElement;
import com.gitee.dorive.api.entity.core.ele.FieldElement;
import com.gitee.dorive.api.entity.core.BindingDefinition;
import com.gitee.dorive.api.entity.core.EntityDefinition;
import com.gitee.dorive.api.entity.core.FieldDefinition;
import com.gitee.dorive.api.entity.core.FieldEntityDefinition;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

@Slf4j
public class EntityElementResolver {

    public List<EntityElement> resolve(EntityDefinition entityDefinition) {
        List<EntityElement> entityElements = new ArrayList<>();

        EntityElement entityElement = resolveElement("/", entityDefinition);
        entityElements.add(entityElement);

        List<FieldEntityDefinition> fieldEntityDefinitions = entityDefinition.getFieldEntityDefinitions();
        for (FieldEntityDefinition fieldEntityDefinition : fieldEntityDefinitions) {
            String fieldName = fieldEntityDefinition.getFieldName();
            EntityElement fieldEntityElement = resolveElement("/" + fieldName, fieldEntityDefinition);
            entityElements.add(fieldEntityElement);
        }

        return entityElements;
    }

    private EntityElement resolveElement(String accessPath, EntityDefinition entityDefinition) {
        EntityDef entityDef = new EntityDef();
        entityDef.setName(entityDefinition.getName());
        entityDef.setSource(ClassUtil.loadClass(entityDefinition.getSourceName()));
        entityDef.setFactory(ClassUtil.loadClass(entityDefinition.getFactoryName()));
        entityDef.setRepository(ClassUtil.loadClass(entityDefinition.getRepositoryName()));
        entityDef.setPriority(entityDefinition.getPriority());
        entityDef.setAutoDiscovery(entityDefinition.isAutoDiscovery());

        List<BindingDefinition> bindingDefinitions = entityDefinition.getBindingDefinitions();
        List<BindingDef> bindingDefs = new ArrayList<>(bindingDefinitions.size());
        for (BindingDefinition bindingDefinition : bindingDefinitions) {
            BindingDef bindingDef = new BindingDef();
            bindingDef.setField(bindingDefinition.getField());
            bindingDef.setValue(bindingDefinition.getValue());
            bindingDef.setBind(bindingDefinition.getBind());
            bindingDef.setExpression(bindingDefinition.getExpression());
            bindingDef.setProcessor(ClassUtil.loadClass(bindingDefinition.getProcessorName()));
            bindingDef.setBindField(bindingDefinition.getBindField());
            bindingDefs.add(bindingDef);
        }

        OrderDef orderDef = new OrderDef();
        orderDef.setSortBy(entityDefinition.getSortBy());
        orderDef.setOrder(entityDefinition.getOrder());

        Class<?> genericType = ClassUtil.loadClass(entityDefinition.getGenericTypeName());

        List<FieldDefinition> fieldDefinitions = entityDefinition.getFieldDefinitions();
        List<FieldElement> fieldElements = new ArrayList<>(fieldDefinitions.size());
        for (FieldDefinition fieldDefinition : fieldDefinitions) {
            FieldElement fieldElement = new FieldElement();
            fieldElement.setFieldDefinition(fieldDefinition);

            FieldDef fieldDef = new FieldDef();
            fieldDef.setPrimary(fieldDefinition.isPrimary());
            fieldDef.setAlias(fieldDefinition.getAlias());
            fieldDef.setValueObj(fieldDefinition.isValueObj());
            fieldDef.setExpression(fieldDefinition.getExpression());
            fieldDef.setConverter(ClassUtil.loadClass(fieldDefinition.getConverterName()));
            fieldElement.setFieldDef(fieldDef);

            fieldElement.setGenericType(ClassUtil.loadClass(fieldDefinition.getGenericTypeName()));
            fieldElements.add(fieldElement);
        }

        Map<String, String> fieldAliasMapping = new LinkedHashMap<>(fieldDefinitions.size() * 4 / 3 + 1);
        for (FieldDefinition fieldDefinition : fieldDefinitions) {
            String fieldName = fieldDefinition.getFieldName();
            String alias = fieldDefinition.getAlias();
            if (StringUtils.isBlank(alias)) {
                alias = StrUtil.toUnderlineCase(fieldName);
            }
            fieldAliasMapping.put(fieldName, alias);
        }

        EntityElement entityElement = new EntityElement();
        entityElement.setEntityDefinition(entityDefinition);
        entityElement.setAccessPath(accessPath);
        entityElement.setEntityDef(entityDef);
        entityElement.setBindingDefs(bindingDefs);
        entityElement.setOrderDef(orderDef);
        entityElement.setGenericType(genericType);
        entityElement.setFieldElements(fieldElements);
        entityElement.setFieldAliasMapping(fieldAliasMapping);
        entityElement.setAttributes(new HashMap<>(4));
        return entityElement;
    }

}
