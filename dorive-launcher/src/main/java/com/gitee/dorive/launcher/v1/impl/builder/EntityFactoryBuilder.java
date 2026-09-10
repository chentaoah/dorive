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
import com.gitee.dorive.base.v1.factory.api.Deserializer;
import com.gitee.dorive.base.v1.factory.api.EntityFactory;
import com.gitee.dorive.base.v1.factory.api.EntityTransformer;
import com.gitee.dorive.base.v1.factory.api.Serializer;
import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import com.gitee.dorive.factory.v1.api.EntityTransformerManager;
import com.gitee.dorive.factory.v1.impl.factory.deserializer.DefaultDeserializer;
import com.gitee.dorive.factory.v1.impl.factory.DefaultEntityFactory;
import com.gitee.dorive.factory.v1.impl.factory.deserializer.ValueObjDeserializer;
import com.gitee.dorive.factory.v1.impl.factory.serializer.DefaultSerializer;
import com.gitee.dorive.factory.v1.impl.factory.serializer.ValueObjSerializer;
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
        RepositoryDef repositoryDef = repositoryContext.getRepositoryDef();
        ApplicationContext applicationContext = repositoryContext.getApplicationContext();
        // 反序列化
        Deserializer deserializer = newDeserializer(repositoryDef, applicationContext);
        // 序列化
        Serializer serializer = newSerializer(repositoryDef, applicationContext);
        // 实体工厂
        return newEntityFactory(repositoryDef, applicationContext, deserializer, serializer);
    }

    @NonNull
    private Deserializer newDeserializer(RepositoryDef repositoryDef, ApplicationContext applicationContext) {
        Class<?> deserializerClass = repositoryDef.getDeserializer();
        Deserializer deserializer;
        if (deserializerClass == Object.class) {
            deserializer = !entityTransformerManager.containValueObj() ? new DefaultDeserializer() : new ValueObjDeserializer();
        } else {
            deserializer = (Deserializer) applicationContext.getBean(deserializerClass);
        }
        if (deserializer instanceof DefaultDeserializer defaultDeserializer) {
            defaultDeserializer.setEntityElement(entityElement);
            defaultDeserializer.setType(reType);
            defaultDeserializer.setEntityTransformer(reEntityTransformer);
        }
        if (deserializer instanceof ValueObjDeserializer valueObjDeserializer) {
            valueObjDeserializer.setEntityTransformerManager(entityTransformerManager);
        }
        if (deserializer instanceof DefaultDeserializer defaultDeserializer) {
            defaultDeserializer.initialize();
        }
        return deserializer;
    }

    @NonNull
    private Serializer newSerializer(RepositoryDef repositoryDef, ApplicationContext applicationContext) {
        Class<?> serializerClass = repositoryDef.getSerializer();
        Serializer serializer;
        if (serializerClass == Object.class) {
            serializer = !entityTransformerManager.containValueObj() ? new DefaultSerializer() : new ValueObjSerializer();
        } else {
            serializer = (Serializer) applicationContext.getBean(serializerClass);
        }
        if (serializer instanceof DefaultSerializer defaultSerializer) {
            defaultSerializer.setType(deType);
            defaultSerializer.setEntityTransformer(deEntityTransformer);
        }
        if (serializer instanceof ValueObjSerializer valueObjSerializer) {
            valueObjSerializer.setEntityTransformerManager(entityTransformerManager);
        }
        if (serializer instanceof DefaultSerializer defaultSerializer) {
            defaultSerializer.initialize();
        }
        return serializer;
    }

    @NonNull
    private EntityFactory newEntityFactory(RepositoryDef repositoryDef, ApplicationContext applicationContext, Deserializer deserializer, Serializer serializer) {
        Class<?> factoryClass = repositoryDef.getFactory();
        EntityFactory entityFactory;
        if (factoryClass == Object.class) {
            entityFactory = new DefaultEntityFactory();
        } else {
            entityFactory = (EntityFactory) applicationContext.getBean(factoryClass);
        }
        // 默认
        if (entityFactory instanceof DefaultEntityFactory defaultEntityFactory) {
            defaultEntityFactory.setDeserializer(deserializer);
            defaultEntityFactory.setSerializer(serializer);
        }
        return entityFactory;
    }

}
