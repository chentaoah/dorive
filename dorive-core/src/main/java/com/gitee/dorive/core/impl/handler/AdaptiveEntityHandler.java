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
package com.gitee.dorive.core.impl.handler;

import com.gitee.dorive.core.api.EntityHandler;
import com.gitee.dorive.core.api.Executor;
import com.gitee.dorive.core.entity.BoundedContext;
import com.gitee.dorive.core.impl.resolver.DelegateResolver;
import com.gitee.dorive.core.repository.AbstractContextRepository;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class AdaptiveEntityHandler implements EntityHandler {

    private final AbstractContextRepository<?, ?> repository;
    private final EntityHandler entityHandler;

    public AdaptiveEntityHandler(AbstractContextRepository<?, ?> repository, EntityHandler entityHandler) {
        this.repository = repository;
        this.entityHandler = entityHandler;
    }

    @Override
    public void handleEntities(BoundedContext boundedContext, List<Object> rootEntities) {
        List<Object> newRootEntities = new ArrayList<>(rootEntities.size());
        int delegateCount = repository.getDelegateResolver().getDelegateCount();
        Map<AbstractContextRepository<?, ?>, List<Object>> repositoryEntitiesMap = new LinkedHashMap<>(delegateCount * 4 / 3 + 1);
        filterRootEntities(rootEntities, newRootEntities, repositoryEntitiesMap);
        if (!newRootEntities.isEmpty()) {
            entityHandler.handleEntities(boundedContext, newRootEntities);
        }
        repositoryEntitiesMap.forEach((repository, entities) -> {
            Executor executor = repository.getExecutor();
            if (executor instanceof EntityHandler) {
                ((EntityHandler) executor).handleEntities(boundedContext, entities);
            }
        });
    }

    private void filterRootEntities(List<Object> rootEntities, List<Object> newRootEntities,
                                    Map<AbstractContextRepository<?, ?>, List<Object>> repositoryEntitiesMap) {
        DelegateResolver delegateResolver = repository.getDelegateResolver();
        for (Object rootEntity : rootEntities) {
            AbstractContextRepository<?, ?> repository = delegateResolver.delegateRepository(rootEntity);
            if (repository == null) {
                newRootEntities.add(rootEntity);
            } else {
                List<Object> existRootEntities = repositoryEntitiesMap.computeIfAbsent(repository, key -> new ArrayList<>(rootEntities.size()));
                existRootEntities.add(rootEntity);
            }
        }
    }

}
