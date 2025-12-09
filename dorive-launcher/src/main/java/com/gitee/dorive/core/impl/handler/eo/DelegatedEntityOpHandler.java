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

package com.gitee.dorive.core.impl.handler.eo;

import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.executor.v1.api.EntityOpHandler;
import com.gitee.dorive.base.v1.core.entity.op.EntityOp;
import com.gitee.dorive.base.v1.core.entity.eop.Delete;
import com.gitee.dorive.base.v1.core.entity.eop.Insert;
import com.gitee.dorive.base.v1.core.entity.eop.InsertOrUpdate;
import com.gitee.dorive.base.v1.core.entity.eop.Update;
import com.gitee.dorive.core.impl.factory.OperationFactory;
import com.gitee.dorive.core.impl.repository.AbstractContextRepository;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class DelegatedEntityOpHandler implements EntityOpHandler {

    private final AbstractContextRepository<?, ?> repository;
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
                entityOpHandler = entityOpHandlerMap.get(repository.getEntityClass());
            }
            if (entityOpHandler != null) {
                totalCount += entityOpHandler.handle(context, buildOperation(entityOp, subEntities));
            }
        }
        return totalCount;
    }

    private EntityOp buildOperation(EntityOp entityOp, List<Object> entities) {
        if (entityOp instanceof Insert) {
            return new Insert(entities);

        } else if (entityOp instanceof Update) {
            return new Update(entities);

        } else if (entityOp instanceof Delete) {
            return new Delete(entities);

        } else if (entityOp instanceof InsertOrUpdate) {
            OperationFactory operationFactory = repository.getOperationFactory();
            return operationFactory.buildInsertOrUpdate(entities);
        }
        return null;
    }

}
