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

import com.gitee.dorive.core.api.executor.EntityHandler;
import com.gitee.dorive.core.api.executor.EntityOpHandler;
import com.gitee.dorive.core.api.executor.Executor;
import com.gitee.dorive.core.repository.AbstractContextRepository;
import lombok.Data;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class DerivedRepositoryResolver {

    private AbstractContextRepository<?, ?> repository;
    private Map<Class<?>, AbstractContextRepository<?, ?>> classRepositoryMap;

    public DerivedRepositoryResolver(AbstractContextRepository<?, ?> repository) {
        this.repository = repository;
    }

    public void resolve() {
        classRepositoryMap = new LinkedHashMap<>(4 * 4 / 3 + 1);
        ReflectionUtils.doWithLocalFields(repository.getClass(), declaredField -> {
            Class<?> fieldClass = declaredField.getType();
            if (AbstractContextRepository.class.isAssignableFrom(fieldClass)) {
                ApplicationContext applicationContext = repository.getApplicationContext();
                Object beanInstance = applicationContext.getBean(fieldClass);
                AbstractContextRepository<?, ?> abstractContextRepository = (AbstractContextRepository<?, ?>) beanInstance;
                Class<?> fieldEntityClass = abstractContextRepository.getEntityType();
                if (repository.getEntityType().isAssignableFrom(fieldEntityClass)) {
                    classRepositoryMap.put(fieldEntityClass, abstractContextRepository);
                }
            }
        });
    }

    public boolean hasDerived() {
        return !classRepositoryMap.isEmpty();
    }

    public Map<Class<?>, EntityHandler> getEntityHandlerMap(EntityHandler entityHandler) {
        int size = classRepositoryMap.size() + 1;
        Map<Class<?>, EntityHandler> entityHandlerMap = new LinkedHashMap<>(size * 4 / 3 + 1);
        entityHandlerMap.put(repository.getEntityType(), entityHandler);
        classRepositoryMap.forEach((clazz, repository) -> {
            Executor executor = repository.getExecutor();
            if (executor instanceof EntityHandler) {
                entityHandlerMap.put(clazz, (EntityHandler) executor);
            }
        });
        return entityHandlerMap;
    }

    public Map<Class<?>, EntityOpHandler> getEntityOpHandlerMap(EntityOpHandler entityOpHandler) {
        int size = classRepositoryMap.size() + 1;
        Map<Class<?>, EntityOpHandler> entityOpHandlerMap = new LinkedHashMap<>(size * 4 / 3 + 1);
        entityOpHandlerMap.put(repository.getEntityType(), entityOpHandler);
        classRepositoryMap.forEach((clazz, repository) -> {
            Executor executor = repository.getExecutor();
            if (executor instanceof EntityOpHandler) {
                entityOpHandlerMap.put(clazz, (EntityOpHandler) executor);
            }
        });
        return entityOpHandlerMap;
    }

}