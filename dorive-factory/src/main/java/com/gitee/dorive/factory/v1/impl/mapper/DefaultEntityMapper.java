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

package com.gitee.dorive.factory.v1.impl.mapper;

import com.gitee.dorive.factory.v1.api.ValueConverter;
import com.gitee.dorive.base.v1.factory.api.entity.EntityMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DefaultEntityMapper implements EntityMapper {

    private Map<String, String> fieldAliasMap = new LinkedHashMap<>();
    private Map<String, String> aliasFieldMap = new LinkedHashMap<>();
    private Map<String, FieldMapping> fieldFieldMappingMap = new LinkedHashMap<>();
    private Map<String, FieldMapping> aliasFieldMappingMap = new LinkedHashMap<>();
    private List<FieldMapping> valueObjFields = new ArrayList<>(4);
    private List<FieldMapping> matchedValueObjFields = new ArrayList<>(4);
    private List<FieldMapping> unmatchedValueObjFields = new ArrayList<>(4);

    public void addField(String field, boolean isMatch, String alias, boolean isValueObj, ValueConverter valueConverter) {
        fieldAliasMap.put(field, alias);
        aliasFieldMap.put(alias, field);

        FieldMapping fieldMapping = new FieldMapping(field, alias, valueConverter);
        fieldFieldMappingMap.put(field, fieldMapping);
        aliasFieldMappingMap.put(alias, fieldMapping);

        if (isValueObj) {
            valueObjFields.add(fieldMapping);
            if (isMatch) {
                matchedValueObjFields.add(fieldMapping);
            } else {
                unmatchedValueObjFields.add(fieldMapping);
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
        FieldMapping fieldMapping = aliasFieldMappingMap.get(name);
        return fieldMapping != null ? fieldMapping.deserialize(value) : value;
    }

    @Override
    public Object serialize(String name, Object value) {
        FieldMapping fieldMapping = fieldFieldMappingMap.get(name);
        return fieldMapping != null ? fieldMapping.serialize(value) : value;
    }

    @Override
    public List<String> serialize(List<String> names) {
        if (names != null && !names.isEmpty()) {
            List<String> aliases = new ArrayList<>(names.size());
            for (String field : names) {
                String alias = serialize(field);
                aliases.add(alias);
            }
            return aliases;
        }
        return names;
    }

    @Override
    public Set<String> serialize(Set<String> names) {
        if (names != null && !names.isEmpty()) {
            Set<String> aliases = new LinkedHashSet<>(names.size() * 4 / 3 + 1);
            for (String field : names) {
                String alias = serialize(field);
                aliases.add(alias);
            }
            return aliases;
        }
        return names;
    }
}
