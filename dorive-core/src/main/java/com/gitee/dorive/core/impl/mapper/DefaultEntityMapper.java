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

package com.gitee.dorive.core.impl.mapper;

import com.gitee.dorive.core.api.mapper.EntityMapper;
import com.gitee.dorive.core.api.mapper.FieldMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Type;
import java.util.*;

@Getter
@AllArgsConstructor
public class DefaultEntityMapper implements EntityMapper {

    private final Map<String, Map<String, String>> mapperFieldAliasMappingMap;
    private final Map<String, FieldMapper> keyFieldMapperMap;
    private final List<FieldMapper> valueObjFields;
    private final List<FieldMapper> matchedValueObjFields;
    private final List<FieldMapper> unmatchedValueObjFields;
    private final Set<Type> valueObjTypes;

    public static String getKey(String mapper, String type, String name) {
        return mapper + ":" + type + ":" + name;
    }

    @Override
    public String toAlias(String mapper, String field) {
        Map<String, String> fieldAliasMapping = mapperFieldAliasMappingMap.get(mapper);
        return fieldAliasMapping.getOrDefault(field, field);
    }

    @Override
    public List<String> toAliases(String mapper, List<String> fields) {
        if (fields != null && !fields.isEmpty()) {
            Map<String, String> fieldAliasMapping = mapperFieldAliasMappingMap.get(mapper);
            List<String> aliases = new ArrayList<>(fields.size());
            for (String field : fields) {
                String alias = fieldAliasMapping.getOrDefault(field, field);
                aliases.add(alias);
            }
            return aliases;
        }
        return fields;
    }

    @Override
    public Set<String> toAliases(String mapper, Set<String> fields) {
        if (fields != null && !fields.isEmpty()) {
            Map<String, String> fieldAliasMapping = mapperFieldAliasMappingMap.get(mapper);
            Set<String> aliases = new LinkedHashSet<>(fields.size() * 4 / 3 + 1);
            for (String field : fields) {
                String alias = fieldAliasMapping.getOrDefault(field, field);
                aliases.add(alias);
            }
            return aliases;
        }
        return fields;
    }

    @Override
    public FieldMapper getMapperByField(String mapper, String field) {
        String key = getKey(mapper, "field", field);
        return keyFieldMapperMap.get(key);
    }

    @Override
    public FieldMapper getMapperByAlias(String mapper, String alias) {
        String key = getKey(mapper, "alias", alias);
        return keyFieldMapperMap.get(key);
    }

    @Override
    public boolean isValueObjType(Type type) {
        return valueObjTypes.contains(type);
    }

}
