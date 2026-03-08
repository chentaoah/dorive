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

package com.gitee.dorive.factory.v1.impl.resolver;

import cn.hutool.core.util.ReflectUtil;
import com.gitee.dorive.base.v1.common.entity.EntityElement;
import com.gitee.dorive.base.v1.common.entity.FieldDefinition;
import com.gitee.dorive.base.v1.common.def.FieldDef;
import com.gitee.dorive.factory.v1.api.Converter;
import com.gitee.dorive.factory.v1.api.EntityTranslator;
import com.gitee.dorive.factory.v1.api.EntityTranslatorManager;
import com.gitee.dorive.factory.v1.api.FieldMapper;
import com.gitee.dorive.factory.v1.impl.mapper.DefaultEntityTranslator;
import com.gitee.dorive.factory.v1.impl.mapper.DefaultEntityTranslatorManager;
import com.gitee.dorive.factory.v1.impl.mapper.DefaultFieldMapper;
import com.gitee.dorive.factory.v1.impl.converter.JsonArrayConverter;
import com.gitee.dorive.factory.v1.impl.converter.JsonConverter;
import com.gitee.dorive.factory.v1.impl.converter.MapConverter;
import com.gitee.dorive.factory.v1.impl.converter.MapExpConverter;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;
import java.util.*;

@Data
@AllArgsConstructor
public class EntityTranslatorManagerResolver {

    private EntityElement entityElement;
    private Map<String, String> aliasPropMapping;
    private String reCategory;
    private String deCategory;

    public EntityTranslatorManager newEntityTranslatorManager() {
        List<FieldDefinition> fieldDefinitions = entityElement.getFieldDefinitions();
        Map<String, String> fieldAliasMapping = entityElement.getFieldAliasMapping();

        // 类别 => EntityTranslator
        Map<String, EntityTranslator> categoryEntityTranslatorMap = new LinkedHashMap<>(4);
        // 全部的值对象字段
        List<FieldMapper> valueObjFields = new ArrayList<>(4);
        // 匹配的值对象字段
        List<FieldMapper> matchedValueObjFields = new ArrayList<>(4);
        // 未匹配的值对象字段
        List<FieldMapper> unmatchedValueObjFields = new ArrayList<>(4);
        // 全部的值对象类型
        Set<Type> valueObjTypes = new HashSet<>(6);

        int size = fieldDefinitions.size() * 4 / 3 + 1;
        // ENTITY_DATABASE
        DefaultEntityTranslator entityTranslator1 = new DefaultEntityTranslator(new LinkedHashMap<>(size), new LinkedHashMap<>(size), new LinkedHashMap<>(size));
        // ENTITY_POJO
        DefaultEntityTranslator entityTranslator2 = new DefaultEntityTranslator(new LinkedHashMap<>(size), new LinkedHashMap<>(size), new LinkedHashMap<>(size));

        for (FieldDefinition fieldDefinition : fieldDefinitions) {
            String field = fieldDefinition.getFieldName();
            String expected = fieldAliasMapping.getOrDefault(field, field);

            boolean isMatch = aliasPropMapping.containsKey(expected);
            String alias = isMatch ? expected : null;
            String prop = isMatch ? aliasPropMapping.get(alias) : null;

            FieldDef fieldDef = fieldDefinition.getFieldDef();
            boolean isValueObj = fieldDef != null && fieldDef.isValueObj();
            Converter converter = newConverter(fieldDefinition, isMatch, isValueObj);

            addToEntityTranslator(entityTranslator1, field, alias, converter);
            addToEntityTranslator(entityTranslator2, field, prop, converter);

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

        categoryEntityTranslatorMap.put(reCategory, entityTranslator1);
        categoryEntityTranslatorMap.put(deCategory, entityTranslator2);

        return new DefaultEntityTranslatorManager(categoryEntityTranslatorMap, valueObjFields, matchedValueObjFields, unmatchedValueObjFields, valueObjTypes);
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

    private void addToEntityTranslator(DefaultEntityTranslator entityTranslator, String field, String alias, Converter converter) {
        entityTranslator.getFieldAliasMapping().put(field, alias);
        FieldMapper fieldMapper = new DefaultFieldMapper(field, alias, converter);
        entityTranslator.getFieldFieldMapperMap().put(field, fieldMapper);
        entityTranslator.getAliasFieldMapperMap().put(alias, fieldMapper);
    }

}
