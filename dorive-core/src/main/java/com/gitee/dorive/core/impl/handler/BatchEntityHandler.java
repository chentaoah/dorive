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
import com.gitee.dorive.core.impl.repository.AbstractContextRepository;
import com.gitee.dorive.core.impl.repository.CommonRepository;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BatchEntityHandler implements EntityHandler {

    private final AbstractContextRepository<?, ?> repository;
    private final List<EntityHandler> entityHandlers;

    public BatchEntityHandler(AbstractContextRepository<?, ?> repository) {
        this.repository = repository;
        List<CommonRepository> subRepositories = repository.getSubRepositories();
        this.entityHandlers = new ArrayList<>(subRepositories.size());
        for (CommonRepository subRepository : subRepositories) {
            EntityHandler entityHandler = new AdaptiveEntityHandler(subRepository);
            if (subRepository.hasValueRouteBinders()) {
                entityHandler = new ValueFilterEntityHandler(subRepository, entityHandler);
            }
            entityHandler = new ContextMatchEntityHandler(subRepository, entityHandler);
            entityHandlers.add(entityHandler);
        }
    }

    @Override
    public long handle(Context context, List<Object> entities) {
        long totalCount = 0L;
        for (EntityHandler entityHandler : entityHandlers) {
            totalCount += entityHandler.handle(context, entities);
        }
        return totalCount;
    }

}
