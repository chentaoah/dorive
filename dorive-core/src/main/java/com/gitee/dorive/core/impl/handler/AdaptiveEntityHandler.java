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

import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.executor.EntityHandler;
import com.gitee.dorive.core.api.executor.Executor;
import com.gitee.dorive.core.impl.resolver.DerivedResolver;
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
    public long handle(Context context, List<Object> entities) {
        List<Object> newEntities = new ArrayList<>(entities.size());
        Map<AbstractContextRepository<?, ?>, List<Object>> repositoryEntitiesMap = distribute(entities, newEntities);

        long totalCount = 0L;
        if (!newEntities.isEmpty()) {
            totalCount += (entityHandler.handle(context, newEntities));
        }
        for (Map.Entry<AbstractContextRepository<?, ?>, List<Object>> entry : repositoryEntitiesMap.entrySet()) {
            AbstractContextRepository<?, ?> repository = entry.getKey();
            List<Object> subclassEntities = entry.getValue();
            Executor executor = repository.getExecutor();
            if (executor instanceof EntityHandler) {
                totalCount += (((EntityHandler) executor).handle(context, subclassEntities));
            }
        }
        return totalCount;
    }

    private Map<AbstractContextRepository<?, ?>, List<Object>> distribute(List<Object> entities, List<Object> newEntities) {
        DerivedResolver derivedResolver = repository.getDerivedResolver();
        int numberOf = derivedResolver.numberOf();
        Map<AbstractContextRepository<?, ?>, List<Object>> repositoryEntitiesMap = new LinkedHashMap<>(numberOf * 4 / 3 + 1);

        for (Object entity : entities) {
            AbstractContextRepository<?, ?> repository = derivedResolver.deriveRepository(entity);
            if (repository == null) {
                newEntities.add(entity);
            } else {
                List<Object> existEntities = repositoryEntitiesMap.computeIfAbsent(repository, key -> new ArrayList<>(entities.size()));
                existEntities.add(entity);
            }
        }

        return repositoryEntitiesMap;
    }

}
