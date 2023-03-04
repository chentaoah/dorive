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
package com.gitee.dorive.spring.boot.starter.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.gitee.dorive.core.api.EntityFactory;
import com.gitee.dorive.core.api.Executor;
import com.gitee.dorive.api.entity.def.EntityDef;
import com.gitee.dorive.core.entity.element.EntityEle;
import com.gitee.dorive.core.impl.AliasConverter;
import com.gitee.dorive.core.impl.DefaultEntityFactory;
import com.gitee.dorive.simple.repository.AbstractSimpleRepository;
import com.gitee.dorive.spring.boot.starter.impl.SQLExampleBuilder;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MybatisPlusRepository<E, PK> extends AbstractSimpleRepository<E, PK> {

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        if ("SQL".equals(querier)) {
            this.exampleBuilder = new SQLExampleBuilder(this);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Executor newExecutor(EntityDef entityDef, EntityEle entityEle) {
        Class<?> mapperClass = entityDef.getSource();
        Object mapper = null;
        Class<?> pojoClass = null;
        if (mapperClass != Object.class) {
            mapper = applicationContext.getBean(mapperClass);
            Type[] genericInterfaces = mapperClass.getGenericInterfaces();
            if (genericInterfaces.length > 0) {
                Type genericInterface = mapperClass.getGenericInterfaces()[0];
                if (genericInterface instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) genericInterface;
                    Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
                    pojoClass = (Class<?>) actualTypeArgument;
                }
            }
        }

        Class<?> factoryClass = entityDef.getFactory();
        EntityFactory entityFactory;
        if (factoryClass == Object.class) {
            entityFactory = new DefaultEntityFactory();
        } else {
            entityFactory = (EntityFactory) applicationContext.getBean(factoryClass);
        }
        if (entityFactory instanceof DefaultEntityFactory) {
            DefaultEntityFactory defaultEntityFactory = (DefaultEntityFactory) entityFactory;
            defaultEntityFactory.setEntityEle(entityEle);
            defaultEntityFactory.setPojoClass(pojoClass);
            defaultEntityFactory.setAliasPropMapping(entityEle.newAliasPropMapping());
            if (pojoClass != null) {
                Map<String, String> aliasPropMapping = entityEle.newAliasPropMapping();
                Map<String, String> propPojoMapping = new LinkedHashMap<>();
                List<TableFieldInfo> fieldList = TableInfoHelper.getTableInfo(pojoClass).getFieldList();
                for (TableFieldInfo tableFieldInfo : fieldList) {
                    String property = tableFieldInfo.getProperty();
                    String column = tableFieldInfo.getColumn();
                    String prop = aliasPropMapping.get(column);
                    if (prop != null) {
                        propPojoMapping.put(prop, property);
                    }
                }
                defaultEntityFactory.setPropPojoMapping(propPojoMapping);
            }
        }

        AliasConverter aliasConverter = new AliasConverter(entityEle);

        return new MybatisPlusExecutor(entityDef, entityEle, (BaseMapper<Object>) mapper, (Class<Object>) pojoClass,
                entityFactory, aliasConverter);
    }

}
