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
import com.gitee.dorive.base.v1.common.def.FieldDef;
import com.gitee.dorive.base.v1.common.entity.EntityElement;
import com.gitee.dorive.base.v1.common.entity.FieldDefinition;
import com.gitee.dorive.factory.v1.api.Converter;
import com.gitee.dorive.factory.v1.api.EntityTransformer;
import com.gitee.dorive.factory.v1.api.EntityTransformerManager;
import com.gitee.dorive.factory.v1.impl.converter.JsonArrayConverter;
import com.gitee.dorive.factory.v1.impl.converter.JsonConverter;
import com.gitee.dorive.factory.v1.impl.converter.MapConverter;
import com.gitee.dorive.factory.v1.impl.converter.MapExpConverter;
import com.gitee.dorive.factory.v1.impl.mapping.DefaultEntityTransformer;
import com.gitee.dorive.factory.v1.impl.mapping.DefaultEntityTransformerManager;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
public class EntityTransformerManagerResolver {

    private EntityElement entityElement;
    private Map<String, String> aliasPropMap;
    private String reCategory;
    private String deCategory;

    public EntityTransformerManager newEntityTransformerManager() {
        List<FieldDefinition> fieldDefinitions = entityElement.getFieldDefinitions();
        Map<String, String> fieldAliasMap = entityElement.getFieldAliasMap();

        // 类别 => EntityTransformer
        Map<String, EntityTransformer> categoryEntityTransformerMap = new LinkedHashMap<>(4);
        Set<Type> valueObjTypes = new HashSet<>(6);
        boolean containMatchedValueObj = false;

        DefaultEntityTransformer reEntityTransformer = new DefaultEntityTransformer();
        DefaultEntityTransformer deEntityTransformer = new DefaultEntityTransformer();

        for (FieldDefinition fieldDefinition : fieldDefinitions) {
            // 字段名称
            String field = fieldDefinition.getFieldName();
            String expected = fieldAliasMap.getOrDefault(field, field);

            // 是否存在别名
            boolean isMatch = aliasPropMap.containsKey(expected);
            String alias = isMatch ? expected : null;
            String prop = isMatch ? aliasPropMap.get(alias) : null;

            // 是否值对象
            FieldDef fieldDef = fieldDefinition.getFieldDef();
            boolean isValueObj = fieldDef != null && fieldDef.isValueObj();
            if (isValueObj) {
                valueObjTypes.add(fieldDefinition.getGenericType());
            }
            // 是否存在匹配的值对象
            if (isValueObj && isMatch) {
                containMatchedValueObj = true;
            }

            // 值转换器
            Converter converter = newConverter(fieldDefinition, isMatch, isValueObj);

            reEntityTransformer.addField(field, isMatch, alias, isValueObj, converter);
            deEntityTransformer.addField(field, isMatch, prop, isValueObj, converter);
        }

        // ENTITY_DATABASE
        categoryEntityTransformerMap.put(reCategory, reEntityTransformer);
        // ENTITY_POJO
        categoryEntityTransformerMap.put(deCategory, deEntityTransformer);

        return new DefaultEntityTransformerManager(categoryEntityTransformerMap, valueObjTypes, containMatchedValueObj);
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

}
