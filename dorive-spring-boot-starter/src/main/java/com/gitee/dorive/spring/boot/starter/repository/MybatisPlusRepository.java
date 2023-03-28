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
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.gitee.dorive.api.entity.def.EntityDef;
import com.gitee.dorive.api.entity.element.EntityEle;
import com.gitee.dorive.core.api.common.EntityFactory;
import com.gitee.dorive.core.api.executor.Executor;
import com.gitee.dorive.core.impl.factory.DefaultEntityFactory;
import com.gitee.dorive.core.impl.factory.OperationFactory;
import com.gitee.dorive.core.repository.AbstractRepository;
import com.gitee.dorive.core.repository.DefaultRepository;
import com.gitee.dorive.simple.repository.AbstractRefRepository;
import com.gitee.dorive.spring.boot.starter.api.Keys;
import com.gitee.dorive.spring.boot.starter.impl.AliasAdapter;
import com.gitee.dorive.spring.boot.starter.impl.SQLExampleBuilder;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MybatisPlusRepository<E, PK> extends AbstractRefRepository<E, PK> {

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        if ("SQL".equals(getQuerier())) {
            setExampleBuilder(new SQLExampleBuilder(this));
        }
    }

    @Override
    protected AbstractRepository<Object, Object> doNewRepository(EntityDef entityDef, EntityEle entityEle, OperationFactory operationFactory) {
        AbstractRepository<Object, Object> repository = super.doNewRepository(entityDef, entityEle, operationFactory);
        if (repository instanceof DefaultRepository) {
            DefaultRepository defaultRepository = (DefaultRepository) repository;
            Executor executor = defaultRepository.getExecutor();
            if (executor instanceof MybatisPlusExecutor) {
                Map<String, Object> attachments = new ConcurrentHashMap<>();

                AliasAdapter aliasAdapter = new AliasAdapter(entityEle);
                attachments.put(Keys.ALIAS_ADAPTER, aliasAdapter);

                MybatisPlusExecutor mybatisPlusExecutor = (MybatisPlusExecutor) executor;
                Class<Object> pojoClass = mybatisPlusExecutor.getPojoClass();
                TableInfo tableInfo = TableInfoHelper.getTableInfo(pojoClass);
                attachments.put(Keys.TABLE_INFO, tableInfo);

                defaultRepository.setAttachments(attachments);
                defaultRepository.setAdapter(aliasAdapter);
            }
        }
        return repository;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Executor newExecutor(EntityDef entityDef, EntityEle entityEle) {
        Class<?> mapperClass = entityDef.getSource();
        Object mapper = null;
        Class<?> pojoClass = null;
        if (mapperClass != Object.class) {
            mapper = getApplicationContext().getBean(mapperClass);
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
            entityFactory = (EntityFactory) getApplicationContext().getBean(factoryClass);
        }
        if (entityFactory instanceof DefaultEntityFactory) {
            DefaultEntityFactory defaultEntityFactory = (DefaultEntityFactory) entityFactory;
            defaultEntityFactory.setEntityEle(entityEle);
            defaultEntityFactory.setPojoClass(pojoClass);
            defaultEntityFactory.setAliasPropMapping(entityEle.newAliasPropMap());
            if (pojoClass != null) {
                Map<String, String> aliasPropMapping = entityEle.newAliasPropMap();
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

        return new MybatisPlusExecutor(entityDef, entityEle, (BaseMapper<Object>) mapper, (Class<Object>) pojoClass, entityFactory);
    }

}
