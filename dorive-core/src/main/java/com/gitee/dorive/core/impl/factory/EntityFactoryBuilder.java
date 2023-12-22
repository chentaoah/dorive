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

import com.gitee.dorive.api.entity.def.EntityDef;
import com.gitee.dorive.api.entity.element.EntityEle;
import com.gitee.dorive.core.api.executor.EntityFactory;
import com.gitee.dorive.core.api.executor.FieldConverter;
import com.gitee.dorive.core.repository.AbstractContextRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.context.ApplicationContext;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class EntityFactoryBuilder {

    private AbstractContextRepository<?, ?> repository;
    private AbstractContextRepository.EntityInfo entityInfo;
    private Map<String, FieldConverter> fieldConverterMap;

    public EntityFactory build(EntityDef entityDef, EntityEle entityEle) {
        Class<?> factoryClass = entityDef.getFactory();
        EntityFactory entityFactory;
        if (factoryClass == Object.class) {
            entityFactory = new DefaultEntityFactory();
        } else {
            ApplicationContext applicationContext = repository.getApplicationContext();
            entityFactory = (EntityFactory) applicationContext.getBean(factoryClass);
        }
        if (entityFactory instanceof DefaultEntityFactory) {
            DefaultEntityFactory defaultEntityFactory = (DefaultEntityFactory) entityFactory;
            defaultEntityFactory.setEntityEle(entityEle);
            defaultEntityFactory.setPojoClass(entityInfo.getPojoClass());

            Map<String, String> aliasFieldMapping = newAliasFieldMapping(entityEle);
            defaultEntityFactory.newReCopyOptions(aliasFieldMapping, fieldConverterMap);

            Map<String, String> fieldPropMapping = newFieldPropMapping(aliasFieldMapping);
            defaultEntityFactory.newDeCopyOptions(fieldPropMapping, newPropConverterMap(fieldPropMapping, fieldConverterMap));
        }
        return entityFactory;
    }

    private Map<String, String> newAliasFieldMapping(EntityEle entityEle) {
        Map<String, String> fieldAliasMap = entityEle.getFieldAliasMap();
        return fieldAliasMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    private Map<String, String> newFieldPropMapping(Map<String, String> aliasFieldMapping) {
        Map<String, String> fieldPropMapping = new LinkedHashMap<>();
        Map<String, String> propAliasMapping = entityInfo.getPropAliasMapping();
        propAliasMapping.forEach((prop, alias) -> {
            String field = aliasFieldMapping.get(alias);
            if (field != null) {
                fieldPropMapping.put(field, prop);
            }
        });
        return fieldPropMapping;
    }

    private Map<String, FieldConverter> newPropConverterMap(Map<String, String> fieldPropMapping, Map<String, FieldConverter> converterMap) {
        Map<String, FieldConverter> propConverterMap = new LinkedHashMap<>(converterMap.size());
        converterMap.forEach((field, fieldConverter) -> {
            String prop = fieldPropMapping.get(field);
            propConverterMap.put(prop, fieldConverter);
        });
        return propConverterMap;
    }

}
