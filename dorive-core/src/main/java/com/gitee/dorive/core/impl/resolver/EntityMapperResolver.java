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
import com.gitee.dorive.api.entity.core.def.FieldDef;
import com.gitee.dorive.api.entity.core.EntityElement;
import com.gitee.dorive.api.entity.core.FieldDefinition;
import com.gitee.dorive.core.api.factory.Converter;
import com.gitee.dorive.core.api.factory.EntityMapper;
import com.gitee.dorive.core.entity.common.EntityStoreInfo;
import com.gitee.dorive.core.entity.enums.Domain;
import com.gitee.dorive.core.entity.factory.FieldConverter;
import com.gitee.dorive.core.impl.converter.JsonArrayConverter;
import com.gitee.dorive.core.impl.converter.JsonConverter;
import com.gitee.dorive.core.impl.converter.MapConverter;
import com.gitee.dorive.core.impl.converter.MapExpConverter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
public class EntityMapperResolver {

    private EntityElement entityElement;
    private EntityStoreInfo entityStoreInfo;

    public EntityMapper newEntityMapper() {
        List<FieldDefinition> fieldDefinitions = entityElement.getFieldDefinitions();
        Map<String, String> aliasPropMapping = entityStoreInfo.getAliasPropMapping();

        Map<String, FieldConverter> fieldConverterMap = new LinkedHashMap<>(fieldDefinitions.size() * 4 / 3 + 1);
        List<FieldConverter> valueObjFields = new ArrayList<>(4);
        List<FieldConverter> matchedValueObjFields = new ArrayList<>(4);
        List<FieldConverter> unmatchedValueObjFields = new ArrayList<>(4);
        Set<Type> valueObjTypes = new HashSet<>(6);

        for (FieldDefinition fieldDefinition : fieldDefinitions) {
            String fieldName = fieldDefinition.getFieldName();

            String expected = entityElement.toAlias(fieldName);
            boolean isMatch = aliasPropMapping.containsKey(expected);
            String alias = isMatch ? expected : null;
            String prop = isMatch ? aliasPropMapping.get(alias) : null;

            Map<String, String> names = new LinkedHashMap<>(5);
            names.put(Domain.ENTITY.name(), fieldName);
            if (alias != null) {
                names.put(Domain.DATABASE.name(), alias);
            }
            if (prop != null) {
                names.put(Domain.POJO.name(), prop);
            }

            FieldDef fieldDef = fieldDefinition.getFieldDef();
            boolean isValueObj = fieldDef != null && fieldDef.isValueObj();
            Converter converter = newConverter(fieldDefinition, isMatch, isValueObj);
            FieldConverter fieldConverter = new FieldConverter(Domain.ENTITY.name(), fieldName, isMatch, names, converter);

            names.forEach((domain, eachName) -> fieldConverterMap.put(getKey(domain, eachName), fieldConverter));
            if (isValueObj) {
                valueObjFields.add(fieldConverter);
                if (isMatch) {
                    matchedValueObjFields.add(fieldConverter);
                } else {
                    unmatchedValueObjFields.add(fieldConverter);
                }
                valueObjTypes.add(fieldDefinition.getGenericType());
            }
        }

        return new DefaultEntityMapper(fieldConverterMap, valueObjFields, matchedValueObjFields, unmatchedValueObjFields, valueObjTypes);
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

    private String getKey(String domain, String name) {
        return domain + ":" + name;
    }

    @Getter
    @AllArgsConstructor
    private class DefaultEntityMapper implements EntityMapper {
        private final Map<String, FieldConverter> fieldConverterMap;
        private final List<FieldConverter> valueObjFields;
        private final List<FieldConverter> matchedValueObjFields;
        private final List<FieldConverter> unmatchedValueObjFields;
        private final Set<Type> valueObjTypes;

        @Override
        public FieldConverter getField(String domain, String name) {
            return fieldConverterMap.get(getKey(domain, name));
        }

        @Override
        public boolean isValueObjType(Type type) {
            return valueObjTypes.contains(type);
        }
    }

}
