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
import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.api.entity.core.EntityDefinition;
import com.gitee.dorive.api.entity.core.EntityElement;
import com.gitee.dorive.api.entity.core.FieldDefinition;
import com.gitee.dorive.api.entity.core.FieldEntityDefinition;
import com.gitee.dorive.api.entity.core.def.BindingDef;
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
        EntityElement entityElement = BeanUtil.copyProperties(entityDefinition, EntityElement.class);

        List<BindingDef> bindingDefs = entityElement.getBindingDefs();
        if (bindingDefs == null) {
            entityElement.setBindingDefs(Collections.emptyList());
        }

        List<FieldDefinition> fieldDefinitions = entityElement.getFieldDefinitions();
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
        entityElement.setAttributes(new HashMap<>(4));
        return entityElement;
    }

}
