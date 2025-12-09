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

import com.gitee.dorive.factory.v1.api.EntityMapper;
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
public class DefaultEntityMapper implements EntityMapper {
    private Map<String, String> fieldAliasMapping;
    private Map<String, FieldMapper> fieldFieldMapperMap;
    private Map<String, FieldMapper> aliasFieldMapperMap;

    @Override
    public String toAlias(String field) {
        return fieldAliasMapping.getOrDefault(field, field);
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
