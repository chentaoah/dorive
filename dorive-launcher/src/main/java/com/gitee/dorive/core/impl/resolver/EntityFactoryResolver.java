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

import com.gitee.dorive.api.entity.core.EntityElement;
import com.gitee.dorive.api.entity.core.def.RepositoryDef;
import com.gitee.dorive.core.api.factory.EntityFactory;
import com.gitee.dorive.core.api.mapper.EntityMapper;
import com.gitee.dorive.core.api.mapper.EntityMappers;
import com.gitee.dorive.core.api.mapper.FieldMapper;
import com.gitee.dorive.core.impl.factory.entity.DefaultEntityFactory;
import com.gitee.dorive.core.impl.factory.entity.ValueObjEntityFactory;
import com.gitee.dorive.core.impl.repository.AbstractContextRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.context.ApplicationContext;

import java.util.List;

@Data
@AllArgsConstructor
public class EntityFactoryResolver {

    private AbstractContextRepository<?, ?> repository;
    private EntityElement entityElement;
    private Class<?> reType;
    private Class<?> deType;
    private EntityMappers entityMappers;
    private EntityMapper reEntityMapper;
    private EntityMapper deEntityMapper;

    public EntityFactory newEntityFactory() {
        RepositoryDef repositoryDef = repository.getRepositoryDef();
        Class<?> factoryClass = repositoryDef.getFactory();
        EntityFactory entityFactory;
        if (factoryClass == Object.class) {
            List<FieldMapper> valueObjFields = entityMappers.getValueObjFields();
            entityFactory = valueObjFields.isEmpty() ? new DefaultEntityFactory() : new ValueObjEntityFactory();
        } else {
            ApplicationContext applicationContext = repository.getApplicationContext();
            entityFactory = (EntityFactory) applicationContext.getBean(factoryClass);
        }
        if (entityFactory instanceof DefaultEntityFactory) {
            DefaultEntityFactory defaultEntityFactory = (DefaultEntityFactory) entityFactory;
            defaultEntityFactory.setEntityElement(entityElement);
            defaultEntityFactory.setReType(entityElement.getGenericType());
            defaultEntityFactory.setDeType(deType);
            defaultEntityFactory.setEntityMappers(entityMappers, reEntityMapper, deEntityMapper);
            defaultEntityFactory.setBoundedContextName(repositoryDef.getBoundedContext());
            defaultEntityFactory.setBoundedContext(repository.getBoundedContext());
        }
        return entityFactory;
    }

}
