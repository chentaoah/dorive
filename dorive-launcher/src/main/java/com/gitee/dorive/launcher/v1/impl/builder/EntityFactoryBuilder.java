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

package com.gitee.dorive.launcher.v1.impl.builder;

import com.gitee.dorive.base.v1.definition.def.RepositoryDef;
import com.gitee.dorive.base.v1.definition.entity.EntityElement;
import com.gitee.dorive.base.v1.factory.api.entity.EntityDeserializer;
import com.gitee.dorive.base.v1.factory.api.entity.EntityFactory;
import com.gitee.dorive.base.v1.factory.api.EntityTransformer;
import com.gitee.dorive.base.v1.factory.api.entity.EntitySerializer;
import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import com.gitee.dorive.factory.v1.api.EntityTransformerManager;
import com.gitee.dorive.factory.v1.impl.factory.deserializer.DefaultEntityDeserializer;
import com.gitee.dorive.factory.v1.impl.factory.DefaultEntityFactory;
import com.gitee.dorive.factory.v1.impl.factory.deserializer.ValueObjEntityDeserializer;
import com.gitee.dorive.factory.v1.impl.factory.serializer.DefaultEntitySerializer;
import com.gitee.dorive.factory.v1.impl.factory.serializer.ValueObjEntitySerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.NonNull;

@Data
@AllArgsConstructor
public class EntityFactoryBuilder {

    private RepositoryContext repositoryContext;
    private EntityElement entityElement;
    private Class<?> reType;
    private Class<?> deType;
    private EntityTransformerManager entityTransformerManager;
    private EntityTransformer reEntityTransformer;
    private EntityTransformer deEntityTransformer;

    public EntityFactory newEntityFactory() {
        // 反序列化
        EntityDeserializer entityDeserializer = newEntityDeserializer();
        // 序列化
        EntitySerializer entitySerializer = newEntitySerializer();
        // 实体工厂
        return newEntityFactory(entityDeserializer, entitySerializer);
    }

    @NonNull
    private EntityDeserializer newEntityDeserializer() {
        RepositoryDef repositoryDef = repositoryContext.getRepositoryDef();
        ApplicationContext applicationContext = repositoryContext.getApplicationContext();

        Class<?> deserializerClass = repositoryDef.getDeserializer();
        EntityDeserializer entityDeserializer;
        if (deserializerClass == Object.class) {
            entityDeserializer = !entityTransformerManager.containValueObj() ? new DefaultEntityDeserializer() : new ValueObjEntityDeserializer();
        } else {
            entityDeserializer = (EntityDeserializer) applicationContext.getBean(deserializerClass);
        }
        if (entityDeserializer instanceof DefaultEntityDeserializer defaultEntityDeserializer) {
            defaultEntityDeserializer.setEntityElement(entityElement);
            defaultEntityDeserializer.setType(reType);
            defaultEntityDeserializer.setEntityTransformer(reEntityTransformer);
        }
        if (entityDeserializer instanceof ValueObjEntityDeserializer valueObjEntityDeserializer) {
            valueObjEntityDeserializer.setEntityTransformerManager(entityTransformerManager);
        }
        if (entityDeserializer instanceof DefaultEntityDeserializer defaultEntityDeserializer) {
            defaultEntityDeserializer.initialize();
        }
        return entityDeserializer;
    }

    @NonNull
    private EntitySerializer newEntitySerializer() {
        RepositoryDef repositoryDef = repositoryContext.getRepositoryDef();
        ApplicationContext applicationContext = repositoryContext.getApplicationContext();

        Class<?> serializerClass = repositoryDef.getSerializer();
        EntitySerializer entitySerializer;
        if (serializerClass == Object.class) {
            entitySerializer = !entityTransformerManager.containValueObj() ? new DefaultEntitySerializer() : new ValueObjEntitySerializer();
        } else {
            entitySerializer = (EntitySerializer) applicationContext.getBean(serializerClass);
        }
        if (entitySerializer instanceof DefaultEntitySerializer defaultEntitySerializer) {
            defaultEntitySerializer.setType(deType);
            defaultEntitySerializer.setEntityTransformer(deEntityTransformer);
        }
        if (entitySerializer instanceof ValueObjEntitySerializer valueObjEntitySerializer) {
            valueObjEntitySerializer.setEntityTransformerManager(entityTransformerManager);
        }
        if (entitySerializer instanceof DefaultEntitySerializer defaultEntitySerializer) {
            defaultEntitySerializer.initialize();
        }
        return entitySerializer;
    }

    @NonNull
    private EntityFactory newEntityFactory(EntityDeserializer entityDeserializer, EntitySerializer entitySerializer) {
        RepositoryDef repositoryDef = repositoryContext.getRepositoryDef();
        ApplicationContext applicationContext = repositoryContext.getApplicationContext();

        Class<?> factoryClass = repositoryDef.getFactory();
        EntityFactory entityFactory;
        if (factoryClass == Object.class) {
            entityFactory = new DefaultEntityFactory();
        } else {
            entityFactory = (EntityFactory) applicationContext.getBean(factoryClass);
        }
        // 默认
        if (entityFactory instanceof DefaultEntityFactory defaultEntityFactory) {
            defaultEntityFactory.setEntityDeserializer(entityDeserializer);
            defaultEntityFactory.setEntitySerializer(entitySerializer);
        }
        return entityFactory;
    }

}
