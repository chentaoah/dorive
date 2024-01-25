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

import com.gitee.dorive.core.repository.AbstractContextRepository;
import lombok.Data;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class DerivedResolver {

    private AbstractContextRepository<?, ?> repository;
    private Map<Class<?>, AbstractContextRepository<?, ?>> classRepositoryMap = new LinkedHashMap<>(3 * 4 / 3 + 1);

    public DerivedResolver(AbstractContextRepository<?, ?> repository) {
        this.repository = repository;
        resolve();
    }

    private void resolve() {
        ReflectionUtils.doWithLocalFields(repository.getClass(), declaredField -> {
            Class<?> fieldClass = declaredField.getType();
            if (AbstractContextRepository.class.isAssignableFrom(fieldClass)) {
                ApplicationContext applicationContext = repository.getApplicationContext();
                Object beanInstance = applicationContext.getBean(fieldClass);
                AbstractContextRepository<?, ?> abstractContextRepository = (AbstractContextRepository<?, ?>) beanInstance;
                Class<?> fieldEntityClass = abstractContextRepository.getEntityClass();
                if (repository.getEntityClass().isAssignableFrom(fieldEntityClass)) {
                    classRepositoryMap.put(fieldEntityClass, abstractContextRepository);
                }
            }
        });
    }

    public boolean hasDerived() {
        return !classRepositoryMap.isEmpty();
    }

    public AbstractContextRepository<?, ?> distribute(Object entity) {
        return classRepositoryMap.getOrDefault(entity.getClass(), repository);
    }

    public Map<AbstractContextRepository<?, ?>, List<Object>> distribute(List<Object> entities) {
        int size = classRepositoryMap.size() + 1;
        Map<AbstractContextRepository<?, ?>, List<Object>> repositoryEntitiesMap = new LinkedHashMap<>(size * 4 / 3 + 1);
        for (Object entity : entities) {
            AbstractContextRepository<?, ?> repository = distribute(entity);
            List<Object> existEntities = repositoryEntitiesMap.computeIfAbsent(repository, key -> new ArrayList<>(entities.size()));
            existEntities.add(entity);
        }
        return repositoryEntitiesMap;
    }

}
