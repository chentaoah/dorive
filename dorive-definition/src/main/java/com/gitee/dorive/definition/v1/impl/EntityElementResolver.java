/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.definition.v1.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.base.v1.definition.def.BindingDef;
import com.gitee.dorive.base.v1.definition.def.EntityDef;
import com.gitee.dorive.base.v1.definition.entity.EntityDefinition;
import com.gitee.dorive.base.v1.definition.entity.EntityElement;
import com.gitee.dorive.base.v1.definition.entity.FieldDefinition;
import com.gitee.dorive.base.v1.definition.entity.FieldEntityDefinition;
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

        Map<String, String> fieldAliasMap = new LinkedHashMap<>(fieldDefinitions.size() * 4 / 3 + 1);
        for (FieldDefinition fieldDefinition : fieldDefinitions) {
            String fieldName = fieldDefinition.getFieldName();
            String alias = fieldDefinition.getAlias();
            if (StringUtils.isBlank(alias)) {
                alias = StrUtil.toUnderlineCase(fieldName);
            }
            fieldAliasMap.put(fieldName, alias);
        }

        entityElement.setAccessPath(accessPath);
        entityElement.setFieldAliasMap(fieldAliasMap);
        return entityElement;
    }

}
