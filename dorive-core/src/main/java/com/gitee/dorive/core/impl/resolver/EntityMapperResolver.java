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
import com.gitee.dorive.api.entity.def.FieldDef;
import com.gitee.dorive.api.entity.element.EntityEle;
import com.gitee.dorive.api.entity.element.EntityField;
import com.gitee.dorive.core.api.converter.Converter;
import com.gitee.dorive.core.api.converter.EntityMapper;
import com.gitee.dorive.core.entity.common.EntityStoreInfo;
import com.gitee.dorive.core.impl.converter.DefaultConverter;
import com.gitee.dorive.core.impl.converter.DefaultEntityMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class EntityMapperResolver {

    private EntityEle entityEle;
    private EntityStoreInfo entityStoreInfo;

    public EntityMapper resolve() {
        Map<String, String> fieldAliasMapping = entityEle.getFieldAliasMapping();
        Map<String, String> aliasFieldMapping = newAliasFieldMapping(fieldAliasMapping);
        Map<String, String> fieldPropMapping = newFieldPropMapping(aliasFieldMapping);

        Map<String, Converter> fieldConverterMap = newFieldConverterMap();
        Map<String, Converter> aliasConverterMap = newAliasConverterMap(fieldAliasMapping, fieldConverterMap);
        Map<String, Converter> propConverterMap = newPropConverterMap(fieldPropMapping, fieldConverterMap);

        return new DefaultEntityMapper(fieldAliasMapping, aliasFieldMapping, fieldPropMapping,
                fieldConverterMap, aliasConverterMap, propConverterMap);
    }

    private Map<String, String> newAliasFieldMapping(Map<String, String> fieldAliasMapping) {
        return fieldAliasMapping.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    private Map<String, String> newFieldPropMapping(Map<String, String> aliasFieldMapping) {
        Map<String, String> fieldPropMapping = new LinkedHashMap<>();
        Map<String, String> propAliasMapping = entityStoreInfo.getPropAliasMapping();
        propAliasMapping.forEach((prop, alias) -> {
            String field = aliasFieldMapping.get(alias);
            if (field != null) {
                fieldPropMapping.put(field, prop);
            }
        });
        return fieldPropMapping;
    }

    private Map<String, Converter> newFieldConverterMap() {
        Map<String, Converter> converterMap = new LinkedHashMap<>(8);
        Map<String, EntityField> entityFieldMap = entityEle.getEntityFieldMap();
        if (entityFieldMap != null) {
            entityFieldMap.forEach((name, entityField) -> {
                FieldDef fieldDef = entityField.getFieldDef();
                if (fieldDef != null) {
                    Class<?> converterClass = fieldDef.getConverter();
                    String mapExp = fieldDef.getMapExp();
                    Converter converter = null;
                    if (converterClass != Object.class) {
                        converter = (Converter) ReflectUtil.newInstance(converterClass);

                    } else if (StringUtils.isNotBlank(mapExp)) {
                        converter = new DefaultConverter(entityField);
                    }
                    if (converter != null) {
                        converterMap.put(name, converter);
                    }
                }
            });
        }
        return converterMap;
    }

    private Map<String, Converter> newAliasConverterMap(Map<String, String> fieldAliasMapping, Map<String, Converter> fieldConverterMap) {
        Map<String, Converter> aliasConverterMap = new LinkedHashMap<>(fieldConverterMap.size());
        fieldConverterMap.forEach((field, converter) -> {
            String alias = fieldAliasMapping.get(field);
            aliasConverterMap.put(alias, converter);
        });
        return aliasConverterMap;
    }

    private Map<String, Converter> newPropConverterMap(Map<String, String> fieldPropMapping, Map<String, Converter> fieldConverterMap) {
        Map<String, Converter> propConverterMap = new LinkedHashMap<>(fieldConverterMap.size());
        fieldConverterMap.forEach((field, converter) -> {
            String prop = fieldPropMapping.get(field);
            propConverterMap.put(prop, converter);
        });
        return propConverterMap;
    }

}
