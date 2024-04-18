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
import com.gitee.dorive.api.def.FieldDef;
import com.gitee.dorive.api.entity.EntityEle;
import com.gitee.dorive.api.entity.EntityField;
import com.gitee.dorive.core.api.factory.Converter;
import com.gitee.dorive.core.api.factory.EntityMapper;
import com.gitee.dorive.core.entity.common.EntityStoreInfo;
import com.gitee.dorive.core.entity.enums.Domain;
import com.gitee.dorive.core.entity.factory.FieldConverter;
import com.gitee.dorive.core.impl.converter.MapExpConverter;
import com.gitee.dorive.core.impl.converter.ValueObjConverter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class EntityMapperResolver {

    private EntityEle entityEle;
    private EntityStoreInfo entityStoreInfo;

    public EntityMapper newEntityMapper() {
        Map<String, EntityField> entityFieldMap = entityEle.getEntityFieldMap();
        Map<String, String> fieldAliasMapping = entityEle.getFieldAliasMapping();
        Map<String, String> aliasPropMapping = entityStoreInfo.getAliasPropMapping();

        Map<String, FieldConverter> fieldConverterMap = new LinkedHashMap<>(entityFieldMap.size() * 4 / 3 + 1);
        List<FieldConverter> valueObjFields = new ArrayList<>(4);
        entityFieldMap.forEach((name, field) -> {
            String alias = fieldAliasMapping.get(name);
            String prop = aliasPropMapping.get(alias);

            Map<String, String> names = new LinkedHashMap<>(5);
            names.put(Domain.ENTITY.name(), name);
            names.put(Domain.DATABASE.name(), alias);
            names.put(Domain.POJO.name(), prop);

            FieldDef fieldDef = field.getFieldDef();
            boolean isValueObj = fieldDef != null && fieldDef.isValueObj();

            Converter converter = newConverter(field, names, isValueObj);
            FieldConverter fieldConverter = new FieldConverter(Domain.ENTITY.name(), name, names, converter);
            names.forEach((domain, eachName) -> fieldConverterMap.put(getKey(domain, eachName), fieldConverter));
            if (isValueObj) {
                valueObjFields.add(fieldConverter);
            }
        });

        return new DefaultEntityMapper(fieldConverterMap, valueObjFields);
    }

    private Converter newConverter(EntityField entityField, Map<String, String> names, boolean isValueObj) {
        FieldDef fieldDef = entityField.getFieldDef();
        if (fieldDef != null) {
            Class<?> converterClass = fieldDef.getConverter();
            if (converterClass != Object.class) {
                return (Converter) ReflectUtil.newInstance(converterClass);

            } else if (isValueObj) {
                String name = names.get(Domain.POJO.name());
                return new ValueObjConverter(entityField.getGenericType(), name != null);

            } else if (StringUtils.isNotBlank(fieldDef.getMapExp())) {
                return new MapExpConverter(entityField);
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

        @Override
        public FieldConverter getField(String domain, String name) {
            return fieldConverterMap.get(getKey(domain, name));
        }

    }

}
