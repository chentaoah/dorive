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

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class DerivedResolver {

    private AbstractContextRepository<?, ?> repository;
    private Map<Class<?>, AbstractContextRepository<?, ?>> derivedRepositoryMap = new LinkedHashMap<>(3 * 4 / 3 + 1);

    public DerivedResolver(AbstractContextRepository<?, ?> repository) {
        this.repository = repository;
        resolve();
    }

    public void resolve() {
        ReflectionUtils.doWithLocalFields(repository.getClass(), declaredField -> {
            Class<?> fieldClass = declaredField.getType();
            if (AbstractContextRepository.class.isAssignableFrom(fieldClass)) {
                ApplicationContext applicationContext = repository.getApplicationContext();
                Object beanInstance = applicationContext.getBean(fieldClass);
                AbstractContextRepository<?, ?> abstractContextRepository = (AbstractContextRepository<?, ?>) beanInstance;
                Class<?> fieldEntityClass = abstractContextRepository.getEntityClass();
                if (repository.getEntityClass().isAssignableFrom(fieldEntityClass)) {
                    derivedRepositoryMap.put(fieldEntityClass, abstractContextRepository);
                }
            }
        });
    }

    public boolean isDerived() {
        return !derivedRepositoryMap.isEmpty();
    }

    public int numberOf() {
        return derivedRepositoryMap.size();
    }

    public AbstractContextRepository<?, ?> deriveRepository(Object entity) {
        return derivedRepositoryMap.get(entity.getClass());
    }

}
