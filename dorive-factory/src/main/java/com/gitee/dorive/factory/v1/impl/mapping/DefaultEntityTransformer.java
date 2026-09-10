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

package com.gitee.dorive.factory.v1.impl.mapping;

import com.gitee.dorive.base.v1.factory.api.ValueConverter;
import com.gitee.dorive.base.v1.factory.api.EntityTransformer;
import com.gitee.dorive.base.v1.factory.api.FieldAliasMapping;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DefaultEntityTransformer implements EntityTransformer {

    private Map<String, String> fieldAliasMap = new LinkedHashMap<>();
    private Map<String, String> aliasFieldMap = new LinkedHashMap<>();
    private Map<String, FieldAliasMapping> fieldFieldAliasMappingMap = new LinkedHashMap<>();
    private Map<String, FieldAliasMapping> aliasFieldAliasMappingMap = new LinkedHashMap<>();
    private List<FieldAliasMapping> valueObjFields = new ArrayList<>(4);
    private List<FieldAliasMapping> matchedValueObjFields = new ArrayList<>(4);
    private List<FieldAliasMapping> unmatchedValueObjFields = new ArrayList<>(4);

    public void addField(String field, boolean isMatch, String alias, boolean isValueObj, ValueConverter valueConverter) {
        fieldAliasMap.put(field, alias);
        aliasFieldMap.put(alias, field);

        FieldAliasMapping fieldAliasMapping = new DefaultFieldAliasMapping(field, alias, valueConverter);
        fieldFieldAliasMappingMap.put(field, fieldAliasMapping);
        aliasFieldAliasMappingMap.put(alias, fieldAliasMapping);

        if (isValueObj) {
            valueObjFields.add(fieldAliasMapping);
            if (isMatch) {
                matchedValueObjFields.add(fieldAliasMapping);
            } else {
                unmatchedValueObjFields.add(fieldAliasMapping);
            }
        }
    }

    @Override
    public String deserialize(String name) {
        return aliasFieldMap.getOrDefault(name, name);
    }

    @Override
    public String serialize(String name) {
        return fieldAliasMap.getOrDefault(name, name);
    }

    @Override
    public Object deserialize(String name, Object value) {
        FieldAliasMapping fieldAliasMapping = aliasFieldAliasMappingMap.get(name);
        return fieldAliasMapping != null ? fieldAliasMapping.deserialize(value) : value;
    }

    @Override
    public Object serialize(String name, Object value) {
        FieldAliasMapping fieldAliasMapping = fieldFieldAliasMappingMap.get(name);
        return fieldAliasMapping != null ? fieldAliasMapping.serialize(value) : value;
    }

    @Override
    public List<String> serialize(List<String> fields) {
        if (fields != null && !fields.isEmpty()) {
            List<String> aliases = new ArrayList<>(fields.size());
            for (String field : fields) {
                String alias = serialize(field);
                aliases.add(alias);
            }
            return aliases;
        }
        return fields;
    }

    @Override
    public Set<String> serialize(Set<String> fields) {
        if (fields != null && !fields.isEmpty()) {
            Set<String> aliases = new LinkedHashSet<>(fields.size() * 4 / 3 + 1);
            for (String field : fields) {
                String alias = serialize(field);
                aliases.add(alias);
            }
            return aliases;
        }
        return fields;
    }
}
