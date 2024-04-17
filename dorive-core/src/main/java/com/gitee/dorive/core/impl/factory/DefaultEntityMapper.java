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

package com.gitee.dorive.core.impl.factory;

import com.gitee.dorive.core.api.factory.Converter;
import com.gitee.dorive.core.api.factory.EntityMapper;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class DefaultEntityMapper implements EntityMapper {

    private Map<String, String> fieldAliasMapping;
    private Map<String, String> aliasFieldMapping;
    private Map<String, String> fieldPropMapping;
    private Map<String, Converter> fieldConverterMap;
    private Map<String, Converter> aliasConverterMap;
    private Map<String, Converter> propConverterMap;

    @Override
    public String fieldToAlias(String field) {
        return fieldAliasMapping.get(field);
    }

    @Override
    public String aliasToField(String alias) {
        return aliasFieldMapping.get(alias);
    }

    @Override
    public String fieldToProp(String field) {
        return fieldPropMapping.get(field);
    }

    @Override
    public boolean hasConverter() {
        return fieldConverterMap != null && !fieldConverterMap.isEmpty();
    }

    @Override
    public Object fieldToAlias(String alias, Object value) {
        Converter converter = aliasConverterMap.get(alias);
        if (converter != null) {
            if (value instanceof List) {
                List<?> list = (List<?>) value;
                List<Object> newList = new ArrayList<>(list.size());
                for (Object item : list) {
                    Object mapValue = converter.deconstruct(item);
                    if (mapValue != null) {
                        newList.add(mapValue);
                    } else {
                        newList.add(item);
                    }
                }
                return newList;

            } else {
                Object mapValue = converter.deconstruct(value);
                if (mapValue != null) {
                    return mapValue;
                }
            }
        }
        return value;
    }

    @Override
    public Object aliasToField(String field, Object value) {
        Converter converter = fieldConverterMap.get(field);
        return converter != null ? converter.reconstitute(value) : value;
    }

    @Override
    public Object fieldToProp(String prop, Object value) {
        Converter converter = propConverterMap.get(prop);
        return converter != null ? converter.deconstruct(value) : value;
    }

}
