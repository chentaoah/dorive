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
package com.gitee.spring.boot.starter.domain.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gitee.spring.boot.starter.domain.impl.SQLExampleConverter;
import com.gitee.spring.domain.coating.repository.AbstractCoatingRepository;
import com.gitee.spring.domain.core.api.EntityFactory;
import com.gitee.spring.domain.core.api.Executor;
import com.gitee.spring.domain.core.entity.EntityElement;
import com.gitee.spring.domain.core.entity.definition.EntityDefinition;
import com.gitee.spring.domain.core.impl.DefaultEntityFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class MybatisPlusRepository<E, PK> extends AbstractCoatingRepository<E, PK> {

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        this.exampleConverter = new SQLExampleConverter(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Executor newExecutor(EntityElement entityElement, EntityDefinition entityDefinition) {
        Class<?> mapperClass = entityDefinition.getMapper();
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

        Class<?> factoryClass = entityDefinition.getFactory();
        EntityFactory entityFactory;
        if (factoryClass == DefaultEntityFactory.class) {
            entityFactory = new DefaultEntityFactory();
        } else {
            entityFactory = (EntityFactory) applicationContext.getBean(factoryClass);
        }
        if (entityFactory instanceof DefaultEntityFactory) {
            DefaultEntityFactory defaultEntityFactory = (DefaultEntityFactory) entityFactory;
            defaultEntityFactory.setEntityElement(entityElement);
            defaultEntityFactory.setPojoClass(pojoClass);
        }

        MybatisPlusExecutor mybatisPlusExecutor = new MybatisPlusExecutor();
        mybatisPlusExecutor.setEntityElement(entityElement);
        mybatisPlusExecutor.setEntityDefinition(entityDefinition);
        mybatisPlusExecutor.setBaseMapper((BaseMapper<Object>) mapper);
        mybatisPlusExecutor.setPojoClass((Class<Object>) pojoClass);
        mybatisPlusExecutor.setEntityFactory(entityFactory);
        return mybatisPlusExecutor;
    }

}
