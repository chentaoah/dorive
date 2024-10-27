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
import com.gitee.dorive.core.repository.AbstractContextRepository;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class DelegatedEntityHandler implements EntityHandler {

    private final AbstractContextRepository<?, ?> repository;
    private final Map<Class<?>, EntityHandler> entityHandlerMap;

    @Override
    public long handle(Context context, List<Object> entities) {
        int size = entityHandlerMap.size();
        Map<Class<?>, List<Object>> subEntitiesMap = new HashMap<>(size * 4 / 3 + 1);
        for (Object entity : entities) {
            Class<?> entityType = entity.getClass();
            List<Object> subEntities = subEntitiesMap.computeIfAbsent(entityType, k -> new ArrayList<>());
            subEntities.add(entity);
        }
        long count = 0L;
        for (Map.Entry<Class<?>, List<Object>> entry : subEntitiesMap.entrySet()) {
            Class<?> entityType = entry.getKey();
            List<Object> subEntities = entry.getValue();
            EntityHandler entityHandler = entityHandlerMap.get(entityType);
            if (entityHandler == null) {
                entityHandler = entityHandlerMap.get(repository.getEntityType());
            }
            if (entityHandler != null) {
                count += entityHandler.handle(context, subEntities);
            }
        }
        return count;
    }

}
