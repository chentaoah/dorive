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

package com.gitee.dorive.api.entity.ele;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReflectUtil;
import com.gitee.dorive.api.entity.EntityDefinition;
import com.gitee.dorive.api.entity.def.BindingDef;
import com.gitee.dorive.api.entity.def.EntityDef;
import com.gitee.dorive.api.entity.def.OrderDef;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EntityElement {

    private EntityDefinition entityDefinition;
    private String accessPath;
    private EntityDef entityDef;
    private List<BindingDef> bindingDefs;
    private OrderDef orderDef;
    private Class<?> genericType;
    private List<FieldElement> fieldElements;
    private Map<String, String> fieldAliasMapping;
    private Map<String, Object> attributes;

    public boolean isCollection() {
        return entityDefinition.isCollection();
    }

    public Object getValue(Object entity) {
        return ReflectUtil.getFieldValue(entity, entityDefinition.getFieldName());
    }

    public void setValue(Object entity, Object value) {
        ReflectUtil.setFieldValue(entity, entityDefinition.getFieldName(), value);
    }

    public String getPrimaryKey() {
        return entityDefinition.getPrimaryKey();
    }

    public Object getPrimaryKey(Object entity) {
        return ReflectUtil.getFieldValue(entity, getPrimaryKey());
    }

    public void setPrimaryKey(Object entity, Object value) {
        ReflectUtil.setFieldValue(entity, getPrimaryKey(), value);
    }

    public boolean hasField(String field) {
        return ReflectUtil.hasField(genericType, field);
    }

    public FieldElement getFieldElement(String fieldName) {
        return CollUtil.findOne(fieldElements, fieldElement -> fieldName.equals(fieldElement.getFieldName()));
    }

    public String toAlias(String field) {
        return fieldAliasMapping.getOrDefault(field, field);
    }

    public List<String> toAliases(List<String> fields) {
        if (fields != null && !fields.isEmpty()) {
            List<String> aliases = new ArrayList<>(fields.size());
            for (String field : fields) {
                String alias = toAlias(field);
                aliases.add(alias);
            }
            return aliases;
        }
        return fields;
    }

    public Set<String> toAliases(Set<String> fields) {
        if (fields != null && !fields.isEmpty()) {
            Set<String> aliases = new LinkedHashSet<>(fields.size() * 4 / 3 + 1);
            for (String field : fields) {
                String alias = toAlias(field);
                aliases.add(alias);
            }
            return aliases;
        }
        return fields;
    }

}
