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
import com.gitee.dorive.core.entity.factory.AliasInfo;
import com.gitee.dorive.core.entity.factory.FieldInfo;
import com.gitee.dorive.core.impl.factory.DefaultConverter;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
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

        Map<String, FieldInfo> fieldInfoMap = new LinkedHashMap<>(entityFieldMap.size() * 4 / 3 + 1);
        entityFieldMap.forEach((name, field) -> {
            FieldInfo fieldInfo = new FieldInfo(Domain.ENTITY.name(), name, new LinkedHashMap<>(3));
            Converter converter = newConverter(field);

            String alias = fieldAliasMapping.get(name);
            AliasInfo databaseAliasInfo = new AliasInfo(Domain.DATABASE.name(), alias, converter);
            fieldInfo.addAliasInfo(databaseAliasInfo);

            String prop = aliasPropMapping.get(alias);
            AliasInfo pojoAliasInfo = new AliasInfo(Domain.POJO.name(), prop, converter);
            fieldInfo.addAliasInfo(pojoAliasInfo);

            fieldInfoMap.put(getKey(fieldInfo.getDomain(), fieldInfo.getName()), fieldInfo);
            fieldInfoMap.put(getKey(databaseAliasInfo.getDomain(), databaseAliasInfo.getName()), fieldInfo);
            fieldInfoMap.put(getKey(pojoAliasInfo.getDomain(), pojoAliasInfo.getName()), fieldInfo);
        });

        return new DefaultEntityMapper(fieldInfoMap);
    }

    private Converter newConverter(EntityField entityField) {
        FieldDef fieldDef = entityField.getFieldDef();
        if (fieldDef != null) {
            Class<?> converterClass = fieldDef.getConverter();
            if (converterClass != Object.class) {
                return (Converter) ReflectUtil.newInstance(converterClass);

            } else if (StringUtils.isNotBlank(fieldDef.getMapExp())) {
                return new DefaultConverter(entityField);
            }
        }
        return null;
    }

    private String getKey(String domain, String name) {
        return domain + ":" + name;
    }

    @AllArgsConstructor
    private class DefaultEntityMapper implements EntityMapper {

        private final Map<String, FieldInfo> fieldInfoMap;

        @Override
        public FieldInfo findField(String domain, String name) {
            return fieldInfoMap.get(getKey(domain, name));
        }

    }

}
