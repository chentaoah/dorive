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
import com.gitee.dorive.core.api.mapper.FieldMapper;
import com.gitee.dorive.core.api.mapper.ValueMapper;
import com.gitee.dorive.core.entity.enums.Mapper;
import com.gitee.dorive.core.impl.mapper.DefaultEntityMapper;
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

import static com.gitee.dorive.core.impl.mapper.DefaultEntityMapper.getKey;

@Data
@AllArgsConstructor
public class EntityMapperResolver {

    private EntityElement entityElement;
    private Map<String, String> aliasPropMapping;

    public EntityMapper newEntityMapper() {
        List<FieldDefinition> fieldDefinitions = entityElement.getFieldDefinitions();

        Map<String, FieldMapper> keyFieldMapperMap = new LinkedHashMap<>(fieldDefinitions.size() * 4 / 3 + 1);
        List<FieldMapper> valueObjFields = new ArrayList<>(4);
        List<FieldMapper> matchedValueObjFields = new ArrayList<>(4);
        List<FieldMapper> unmatchedValueObjFields = new ArrayList<>(4);
        Set<Type> valueObjTypes = new HashSet<>(6);

        for (FieldDefinition fieldDefinition : fieldDefinitions) {
            String field = fieldDefinition.getFieldName();
            String expected = entityElement.toAlias(field);

            boolean isMatch = aliasPropMapping.containsKey(expected);
            String alias = isMatch ? expected : null;
            String prop = isMatch ? aliasPropMapping.get(alias) : null;

            FieldDef fieldDef = fieldDefinition.getFieldDef();
            boolean isValueObj = fieldDef != null && fieldDef.isValueObj();
            ValueMapper valueMapper = newValueMapper(fieldDefinition, isMatch, isValueObj);

            FieldMapper fieldMapper1 = newFieldMapper(keyFieldMapperMap, Mapper.ENTITY_DATABASE.name(), field, alias, valueMapper);
            FieldMapper fieldMapper2 = newFieldMapper(keyFieldMapperMap, Mapper.ENTITY_POJO.name(), field, prop, valueMapper);

            handleValueObjMapper(valueObjFields, matchedValueObjFields, unmatchedValueObjFields, valueObjTypes, fieldDefinition, isMatch, isValueObj, fieldMapper1);
            handleValueObjMapper(valueObjFields, matchedValueObjFields, unmatchedValueObjFields, valueObjTypes, fieldDefinition, isMatch, isValueObj, fieldMapper2);
        }

        return new DefaultEntityMapper(keyFieldMapperMap, valueObjFields, matchedValueObjFields, unmatchedValueObjFields, valueObjTypes);
    }

    private ValueMapper newValueMapper(FieldDefinition fieldDefinition, boolean isMatch, boolean isValueObj) {
        FieldDef fieldDef = fieldDefinition.getFieldDef();
        if (fieldDef != null) {
            Class<?> converterClass = fieldDef.getConverter();
            if (converterClass != Object.class) {
                return (ValueMapper) ReflectUtil.newInstance(converterClass);

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

    private FieldMapper newFieldMapper(Map<String, FieldMapper> keyFieldMapperMap, String mapper, String field, String alias, ValueMapper valueMapper) {
        FieldMapper fieldMapper = new DefaultFieldMapper(mapper, field, alias, valueMapper);
        keyFieldMapperMap.put(getKey(mapper, "field", field), fieldMapper);
        keyFieldMapperMap.put(getKey(mapper, "alias", alias), fieldMapper);
        return fieldMapper;
    }

    private void handleValueObjMapper(List<FieldMapper> valueObjFields, List<FieldMapper> matchedValueObjFields, List<FieldMapper> unmatchedValueObjFields, Set<Type> valueObjTypes,
                                      FieldDefinition fieldDefinition, boolean isMatch, boolean isValueObj, FieldMapper fieldMapper) {
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

}
