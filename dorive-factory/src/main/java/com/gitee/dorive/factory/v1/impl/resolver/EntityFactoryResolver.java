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

import com.gitee.dorive.base.v1.common.def.RepositoryDef;
import com.gitee.dorive.base.v1.common.entity.EntityElement;
import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import com.gitee.dorive.factory.v1.api.EntityFactory;
import com.gitee.dorive.factory.v1.api.EntityTranslator;
import com.gitee.dorive.factory.v1.api.EntityTranslatorManager;
import com.gitee.dorive.factory.v1.impl.factory.ContextEntityFactory;
import com.gitee.dorive.factory.v1.impl.factory.DefaultEntityFactory;
import com.gitee.dorive.factory.v1.impl.factory.ValueObjEntityFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.context.ApplicationContext;

@Data
@AllArgsConstructor
public class EntityFactoryResolver {

    private RepositoryContext repository;
    private EntityElement entityElement;
    private Class<?> reType;
    private Class<?> deType;
    private EntityTranslatorManager entityTranslatorManager;
    private EntityTranslator reEntityTranslator;
    private EntityTranslator deEntityTranslator;

    public EntityFactory newEntityFactory() {
        RepositoryDef repositoryDef = repository.getRepositoryDef();
        Class<?> factoryClass = repositoryDef.getFactory();
        EntityFactory entityFactory;
        if (factoryClass == Object.class) {
            entityFactory = !entityTranslatorManager.containValueObj() ? new DefaultEntityFactory() : new ValueObjEntityFactory();
        } else {
            ApplicationContext applicationContext = repository.getApplicationContext();
            entityFactory = (EntityFactory) applicationContext.getBean(factoryClass);
        }
        // 默认
        if (entityFactory instanceof DefaultEntityFactory) {
            DefaultEntityFactory defaultEntityFactory = (DefaultEntityFactory) entityFactory;
            defaultEntityFactory.setEntityElement(entityElement);
            defaultEntityFactory.setReType(reType);
            defaultEntityFactory.setDeType(deType);
            defaultEntityFactory.setReEntityTranslator(reEntityTranslator);
            defaultEntityFactory.setDeEntityTranslator(deEntityTranslator);
        }
        // 值对象
        if (entityFactory instanceof ValueObjEntityFactory) {
            ValueObjEntityFactory valueObjEntityFactory = (ValueObjEntityFactory) entityFactory;
            valueObjEntityFactory.setEntityTranslatorManager(entityTranslatorManager);
        }
        // 初始化
        if (entityFactory instanceof DefaultEntityFactory) {
            ((DefaultEntityFactory) entityFactory).initialize();
        }
        // 边界上下文实体工厂
        ContextEntityFactory contextEntityFactory = new ContextEntityFactory();
        contextEntityFactory.setBoundedContextName(repositoryDef.getBoundedContext());
        contextEntityFactory.setBoundedContext(repository.getBoundedContext());
        contextEntityFactory.initCtxCopyOptions(entityElement);
        contextEntityFactory.setEntityFactory(entityFactory);
        return contextEntityFactory;
    }

}
