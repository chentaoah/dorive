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

package com.gitee.dorive.core.impl.resolver;

import cn.hutool.core.util.ReflectUtil;
import com.gitee.dorive.api.entity.core.EntityElement;
import com.gitee.dorive.api.entity.core.FieldDefinition;
import com.gitee.dorive.api.entity.core.def.FieldDef;
import com.gitee.dorive.core.api.mapper.EntityMapper;
import com.gitee.dorive.core.api.mapper.EntityMappers;
import com.gitee.dorive.core.api.mapper.FieldMapper;
import com.gitee.dorive.core.api.mapper.Converter;
import com.gitee.dorive.core.impl.mapper.DefaultEntityMapper;
import com.gitee.dorive.core.impl.mapper.DefaultEntityMappers;
import com.gitee.dorive.core.impl.mapper.DefaultFieldMapper;
import com.gitee.dorive.core.impl.mapper.value.JsonArrayConverter;
import com.gitee.dorive.core.impl.mapper.value.JsonConverter;
import com.gitee.dorive.core.impl.mapper.value.MapConverter;
import com.gitee.dorive.core.impl.mapper.value.MapExpConverter;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;
import java.util.*;

@Data
@AllArgsConstructor
public class EntityMappersResolver {

    private EntityElement entityElement;
    private Map<String, String> aliasPropMapping;
    private String reMapper;
    private String deMapper;

    public EntityMappers newEntityMappers() {
        List<FieldDefinition> fieldDefinitions = entityElement.getFieldDefinitions();
        Map<String, String> fieldAliasMapping = entityElement.getFieldAliasMapping();

        Map<String, EntityMapper> mapperEntityMapperMap = new LinkedHashMap<>(4);
        List<FieldMapper> valueObjFields = new ArrayList<>(4);
        List<FieldMapper> matchedValueObjFields = new ArrayList<>(4);
        List<FieldMapper> unmatchedValueObjFields = new ArrayList<>(4);
        Set<Type> valueObjTypes = new HashSet<>(6);

        int size = fieldDefinitions.size() * 4 / 3 + 1;
        DefaultEntityMapper entityMapper1 = new DefaultEntityMapper(new LinkedHashMap<>(size), new LinkedHashMap<>(size), new LinkedHashMap<>(size));
        DefaultEntityMapper entityMapper2 = new DefaultEntityMapper(new LinkedHashMap<>(size), new LinkedHashMap<>(size), new LinkedHashMap<>(size));

        for (FieldDefinition fieldDefinition : fieldDefinitions) {
            String field = fieldDefinition.getFieldName();
            String expected = fieldAliasMapping.getOrDefault(field, field);

            boolean isMatch = aliasPropMapping.containsKey(expected);
            String alias = isMatch ? expected : null;
            String prop = isMatch ? aliasPropMapping.get(alias) : null;

            FieldDef fieldDef = fieldDefinition.getFieldDef();
            boolean isValueObj = fieldDef != null && fieldDef.isValueObj();
            Converter converter = newConverter(fieldDefinition, isMatch, isValueObj);

            addToEntityMapper(entityMapper1, field, alias, converter);
            addToEntityMapper(entityMapper2, field, prop, converter);

            FieldMapper fieldMapper = new DefaultFieldMapper(field, alias, converter);
            if (isValueObj) {
                valueObjFields.add(fieldMapper);
                if (isMatch) {
                    matchedValueObjFields.add(fieldMapper);
                } else {
                    unmatchedValueObjFields.add(fieldMapper);
                }
                valueObjTypes.add(fieldDefinition.getGenericType());
            }
        }

        mapperEntityMapperMap.put(reMapper, entityMapper1);
        mapperEntityMapperMap.put(deMapper, entityMapper2);

        return new DefaultEntityMappers(mapperEntityMapperMap, valueObjFields, matchedValueObjFields, unmatchedValueObjFields, valueObjTypes);
    }

    private Converter newConverter(FieldDefinition fieldDefinition, boolean isMatch, boolean isValueObj) {
        FieldDef fieldDef = fieldDefinition.getFieldDef();
        if (fieldDef != null) {
            Class<?> converterClass = fieldDef.getConverter();
            if (converterClass != Object.class) {
                return (Converter) ReflectUtil.newInstance(converterClass);

            } else if (isValueObj) {
                Class<?> genericType = fieldDefinition.getGenericType();
                if (isMatch) {
                    return !fieldDefinition.isCollection() ? new JsonConverter(genericType) : new JsonArrayConverter(genericType);
                } else {
                    return new MapConverter(genericType);
                }

            } else if (StringUtils.isNotBlank(fieldDef.getExpression())) {
                return new MapExpConverter(fieldDefinition);
            }
        }
        return null;
    }

    private void addToEntityMapper(DefaultEntityMapper entityMapper, String field, String alias, Converter converter) {
        entityMapper.getFieldAliasMapping().put(field, alias);
        FieldMapper fieldMapper = new DefaultFieldMapper(field, alias, converter);
        entityMapper.getFieldFieldMapperMap().put(field, fieldMapper);
        entityMapper.getAliasFieldMapperMap().put(alias, fieldMapper);
    }

}
