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

import com.gitee.dorive.factory.v1.api.Converter;
import com.gitee.dorive.factory.v1.api.EntityTranslator;
import com.gitee.dorive.factory.v1.api.FieldMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DefaultEntityTranslator implements EntityTranslator {

    private Map<String, String> fieldAliasMap = new LinkedHashMap<>();
    private Map<String, String> aliasFieldMap = new LinkedHashMap<>();
    private Map<String, FieldMapper> fieldFieldMapperMap = new LinkedHashMap<>();
    private Map<String, FieldMapper> aliasFieldMapperMap = new LinkedHashMap<>();
    private List<FieldMapper> valueObjFields = new ArrayList<>(4);
    private List<FieldMapper> matchedValueObjFields = new ArrayList<>(4);
    private List<FieldMapper> unmatchedValueObjFields = new ArrayList<>(4);

    public void addField(String field, boolean isMatch, String alias, boolean isValueObj, Converter converter) {
        fieldAliasMap.put(field, alias);
        aliasFieldMap.put(alias, field);

        FieldMapper fieldMapper = new DefaultFieldMapper(field, alias, converter);
        fieldFieldMapperMap.put(field, fieldMapper);
        aliasFieldMapperMap.put(alias, fieldMapper);

        if (isValueObj) {
            valueObjFields.add(fieldMapper);
            if (isMatch) {
                matchedValueObjFields.add(fieldMapper);
            } else {
                unmatchedValueObjFields.add(fieldMapper);
            }
        }
    }

    @Override
    public String toAlias(String field) {
        return fieldAliasMap.getOrDefault(field, field);
    }

    @Override
    public String toField(String alias) {
        return aliasFieldMap.getOrDefault(alias, alias);
    }

    @Override
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

    @Override
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

    @Override
    public FieldMapper getFieldMapperByField(String field) {
        return fieldFieldMapperMap.get(field);
    }

    @Override
    public FieldMapper getFieldMapperByAlias(String alias) {
        return aliasFieldMapperMap.get(alias);
    }
}
