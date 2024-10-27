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

import cn.hutool.core.lang.Pair;
import com.gitee.dorive.core.api.executor.EntityHandler;
import com.gitee.dorive.core.api.executor.Executor;
import com.gitee.dorive.core.repository.AbstractContextRepository;
import lombok.Data;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class DerivedRepositoryResolver {

    private AbstractContextRepository<?, ?> repository;
    private Map<Class<?>, AbstractContextRepository<?, ?>> classRepositoryMap = new LinkedHashMap<>(3 * 4 / 3 + 1);

    public DerivedRepositoryResolver(AbstractContextRepository<?, ?> repository) {
        this.repository = repository;
    }

    public void resolve() {
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
        Map<Class<?>, EntityHandler> entityHandlerMap = new LinkedHashMap<>();
        entityHandlerMap.put(repository.getEntityType(), entityHandler);
        classRepositoryMap.forEach((clazz, repository) -> {
            Executor executor = repository.getExecutor();
            if (executor instanceof EntityHandler) {
                entityHandlerMap.put(clazz, (EntityHandler) executor);
            }
        });
        return entityHandlerMap;
    }

    public Collection<Pair<AbstractContextRepository<?, ?>, List<Object>>> distribute(List<?> entities) {
        int size = classRepositoryMap.size() + 1;
        Map<Class<?>, Pair<AbstractContextRepository<?, ?>, List<Object>>> classRepoEntitiesPairMap = new HashMap<>(size * 4 / 3 + 1);
        for (Object entity : entities) {
            Class<?> clazz = entity.getClass();
            Pair<AbstractContextRepository<?, ?>, List<Object>> repoEntitiesPair = classRepoEntitiesPairMap.computeIfAbsent(clazz, key -> {
                AbstractContextRepository<?, ?> repository = classRepositoryMap.getOrDefault(key, this.repository);
                List<Object> partEntities = new ArrayList<>(entities.size());
                return new Pair<>(repository, partEntities);
            });
            List<Object> partEntities = repoEntitiesPair.getValue();
            partEntities.add(entity);
        }
        return classRepoEntitiesPairMap.values();
    }

}
