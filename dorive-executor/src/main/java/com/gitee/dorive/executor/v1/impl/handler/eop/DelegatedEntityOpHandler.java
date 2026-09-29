/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.executor.v1.impl.handler.eop;

import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.base.v1.executor.entity.eop.Delete;
import com.gitee.dorive.base.v1.executor.entity.eop.Insert;
import com.gitee.dorive.base.v1.executor.entity.eop.InsertOrUpdate;
import com.gitee.dorive.base.v1.executor.entity.eop.Update;
import com.gitee.dorive.base.v1.executor.entity.eop.EntityOp;
import com.gitee.dorive.base.v1.executor.api.OperationFactory;
import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import com.gitee.dorive.base.v1.executor.api.EntityOpHandler;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class DelegatedEntityOpHandler implements EntityOpHandler {

    private final RepositoryContext repositoryContext;
    private final Map<Class<?>, EntityOpHandler> entityOpHandlerMap;

    @Override
    public long handle(Context context, EntityOp entityOp) {
        List<?> entities = entityOp.getEntities();
        int size = entityOpHandlerMap.size();
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
            EntityOpHandler entityOpHandler = entityOpHandlerMap.get(entityType);
            if (entityOpHandler == null) {
                entityOpHandler = entityOpHandlerMap.get(repositoryContext.getEntityClass());
            }
            if (entityOpHandler != null) {
                totalCount += entityOpHandler.handle(context, buildOperation(entityOp, subEntities));
            }
        }
        return totalCount;
    }

    private EntityOp buildOperation(EntityOp entityOp, List<Object> entities) {
        OperationFactory operationFactory = repositoryContext.getOperationFactory();
        if (entityOp instanceof Insert) {
            return (EntityOp) operationFactory.buildInsert(entities);

        } else if (entityOp instanceof Update) {
            return (EntityOp) operationFactory.buildUpdate(entities);

        } else if (entityOp instanceof Delete) {
            return (EntityOp) operationFactory.buildDelete(entities);

        } else if (entityOp instanceof InsertOrUpdate) {
            return (EntityOp) operationFactory.buildInsertOrUpdate(entities);
        }
        return null;
    }

}
