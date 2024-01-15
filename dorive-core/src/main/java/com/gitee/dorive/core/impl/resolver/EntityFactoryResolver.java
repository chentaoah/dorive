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

import com.gitee.dorive.api.entity.def.EntityDef;
import com.gitee.dorive.api.entity.element.EntityEle;
import com.gitee.dorive.core.api.executor.EntityFactory;
import com.gitee.dorive.core.api.executor.FieldsMapper;
import com.gitee.dorive.core.impl.factory.DefaultEntityFactory;
import com.gitee.dorive.core.repository.AbstractContextRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.context.ApplicationContext;

@Data
@AllArgsConstructor
public class EntityFactoryResolver {

    private AbstractContextRepository<?, ?> repository;
    private AbstractContextRepository.EntityInfo entityInfo;
    private FieldsMapper fieldsMapper;

    public EntityFactory resolve(EntityDef entityDef, EntityEle entityEle) {
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
            defaultEntityFactory.setFieldsMapper(fieldsMapper);
        }
        return entityFactory;
    }

}
