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

package com.gitee.dorive.aggregate.v1.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.base.v1.common.def.BindingDef;
import com.gitee.dorive.base.v1.common.def.EntityDef;
import com.gitee.dorive.base.v1.common.entity.EntityDefinition;
import com.gitee.dorive.base.v1.common.entity.EntityElement;
import com.gitee.dorive.base.v1.common.entity.FieldDefinition;
import com.gitee.dorive.base.v1.common.entity.FieldEntityDefinition;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

@Slf4j
public class EntityElementResolver {

    public List<EntityElement> resolve(EntityDefinition entityDefinition) {
        List<EntityElement> entityElements = new ArrayList<>();
        // 类
        EntityElement entityElement = resolveElement("/", entityDefinition);
        entityElements.add(entityElement);
        // 字段
        List<FieldEntityDefinition> fieldEntityDefinitions = entityDefinition.getFieldEntityDefinitions();
        for (FieldEntityDefinition fieldEntityDefinition : fieldEntityDefinitions) {
            String fieldName = fieldEntityDefinition.getFieldName();
            EntityElement fieldEntityElement = resolveElement("/" + fieldName, fieldEntityDefinition);
            entityElements.add(fieldEntityElement);
        }
        return entityElements;
    }

    private EntityElement resolveElement(String accessPath, EntityDefinition entityDefinition) {
        EntityElement entityElement = BeanUtil.copyProperties(entityDefinition, EntityElement.class);
        // 深拷贝可能被重置的注解定义，以防影响原对象
        entityElement.setEntityDef(BeanUtil.copyProperties(entityElement.getEntityDef(), EntityDef.class));
        entityElement.setBindingDefs(BeanUtil.copyToList(entityElement.getBindingDefs(), BindingDef.class));

        List<FieldDefinition> fieldDefinitions = entityElement.getFieldDefinitions();
        List<BindingDef> bindingDefs = entityElement.getBindingDefs();

        if (bindingDefs == null) {
            entityElement.setBindingDefs(Collections.emptyList());
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

        entityElement.setAccessPath(accessPath);
        entityElement.setFieldAliasMapping(fieldAliasMapping);
        return entityElement;
    }

}
