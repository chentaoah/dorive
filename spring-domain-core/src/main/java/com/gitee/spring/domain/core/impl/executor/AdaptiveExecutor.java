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
package com.gitee.spring.domain.core.impl.executor;

import com.gitee.spring.domain.core.api.EntityHandler;
import com.gitee.spring.domain.core.api.Executor;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.impl.resolver.DelegateResolver;
import com.gitee.spring.domain.core.repository.AbstractContextRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AdaptiveExecutor extends ChainExecutor {

    public AdaptiveExecutor(AbstractContextRepository<?, ?> repository, EntityHandler entityHandler) {
        super(repository, entityHandler);
    }

    @Override
    public void handleEntities(BoundedContext boundedContext, List<Object> rootEntities) {
        Map<AbstractContextRepository<?, ?>, List<Object>> repositoryEntitiesMap = collectRepositoryEntitiesMap(rootEntities);
        repositoryEntitiesMap.forEach((repository, entities) -> {
            Executor executor = repository.getExecutor();
            if (executor instanceof ChainExecutor) {
                ((ChainExecutor) executor).doHandleEntities(boundedContext, entities);
            }
        });
    }

    private Map<AbstractContextRepository<?, ?>, List<Object>> collectRepositoryEntitiesMap(List<Object> rootEntities) {
        DelegateResolver delegateResolver = getRepository().getDelegateResolver();
        int delegateCount = delegateResolver.getDelegateCount();
        Map<AbstractContextRepository<?, ?>, List<Object>> repositoryEntitiesMap = new LinkedHashMap<>(delegateCount * 4 / 3 + 1);
        for (Object rootEntity : rootEntities) {
            AbstractContextRepository<?, ?> repository = delegateResolver.delegateRepository(rootEntity);
            List<Object> entities = repositoryEntitiesMap.computeIfAbsent(repository, key -> new ArrayList<>(rootEntities.size()));
            entities.add(rootEntity);
        }
        return repositoryEntitiesMap;
    }

}
