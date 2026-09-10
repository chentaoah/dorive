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
import com.gitee.dorive.factory.v1.impl.factory.DefaultDeserializer;
import com.gitee.dorive.factory.v1.impl.factory.DefaultEntityFactory;
import com.gitee.dorive.factory.v1.impl.factory.DefaultSerializer;
import com.gitee.dorive.factory.v1.impl.factory.ValueObjEntityFactory;
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
            deserializer = new DefaultDeserializer();
        } else {
            deserializer = (Deserializer) applicationContext.getBean(deserializerClass);
        }
        if (deserializer instanceof DefaultDeserializer defaultDeserializer) {
            defaultDeserializer.setEntityElement(entityElement);
            defaultDeserializer.setReType(reType);
            defaultDeserializer.setReEntityTransformer(reEntityTransformer);
            defaultDeserializer.initialize();
        }
        return deserializer;
    }

    @NonNull
    private Serializer newSerializer(RepositoryDef repositoryDef, ApplicationContext applicationContext) {
        Class<?> serializerClass = repositoryDef.getSerializer();
        Serializer serializer;
        if (serializerClass == Object.class) {
            serializer = new DefaultSerializer();
        } else {
            serializer = (Serializer) applicationContext.getBean(serializerClass);
        }
        if (serializer instanceof DefaultSerializer defaultSerializer) {
            defaultSerializer.setDeType(deType);
            defaultSerializer.setDeEntityTransformer(deEntityTransformer);
            defaultSerializer.initialize();
        }
        return serializer;
    }

    @NonNull
    private EntityFactory newEntityFactory(RepositoryDef repositoryDef, ApplicationContext applicationContext, Deserializer deserializer, Serializer serializer) {
        Class<?> factoryClass = repositoryDef.getFactory();
        EntityFactory entityFactory;
        if (factoryClass == Object.class) {
            entityFactory = !entityTransformerManager.containValueObj() ? //
                    new DefaultEntityFactory() : new ValueObjEntityFactory();
        } else {
            entityFactory = (EntityFactory) applicationContext.getBean(factoryClass);
        }
        // 默认
        if (entityFactory instanceof DefaultEntityFactory defaultEntityFactory) {
            defaultEntityFactory.setDeserializer(deserializer);
            defaultEntityFactory.setSerializer(serializer);
        }
        // 值对象
        if (entityFactory instanceof ValueObjEntityFactory valueObjEntityFactory) {
            valueObjEntityFactory.setEntityTransformerManager(entityTransformerManager);
            valueObjEntityFactory.initialize();
        }
        return entityFactory;
    }

}
