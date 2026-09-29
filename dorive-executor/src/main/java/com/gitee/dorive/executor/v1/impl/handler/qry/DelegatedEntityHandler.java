/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.executor.v1.impl.handler.qry;

import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.base.v1.executor.api.EntityHandler;
import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class DelegatedEntityHandler implements EntityHandler {

    private final RepositoryContext repositoryContext;
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
        long totalCount = 0L;
        for (Map.Entry<Class<?>, List<Object>> entry : subEntitiesMap.entrySet()) {
            Class<?> entityType = entry.getKey();
            List<Object> subEntities = entry.getValue();
            EntityHandler entityHandler = entityHandlerMap.get(entityType);
            if (entityHandler == null) {
                entityHandler = entityHandlerMap.get(repositoryContext.getEntityClass());
            }
            if (entityHandler != null) {
                totalCount += entityHandler.handle(context, subEntities);
            }
        }
        return totalCount;
    }

}
