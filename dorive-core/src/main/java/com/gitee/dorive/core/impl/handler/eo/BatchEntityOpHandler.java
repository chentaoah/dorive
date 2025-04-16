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

import com.gitee.dorive.api.entity.core.EntityElement;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.executor.EntityOpHandler;
import com.gitee.dorive.core.entity.operation.EntityOp;
import com.gitee.dorive.core.entity.operation.Operation;
import com.gitee.dorive.core.entity.operation.eop.Delete;
import com.gitee.dorive.core.entity.operation.eop.Insert;
import com.gitee.dorive.core.entity.operation.eop.InsertOrUpdate;
import com.gitee.dorive.core.entity.operation.eop.Update;
import com.gitee.dorive.core.impl.factory.OperationFactory;
import com.gitee.dorive.core.impl.repository.AbstractContextRepository;
import com.gitee.dorive.core.impl.repository.CommonRepository;
import com.gitee.dorive.core.util.CollectionUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BatchEntityOpHandler implements EntityOpHandler {

    private final AbstractContextRepository<?, ?> repository;

    @Override
    public long handle(Context context, EntityOp entityOp) {
        int totalCount = 0;
        if (entityOp instanceof Insert) {
            totalCount += executeInsert(context, entityOp);

        } else if (entityOp instanceof Update) {
            totalCount += executeUpdateOrDelete(context, entityOp);

        } else if (entityOp instanceof Delete) {
            totalCount += executeUpdateOrDelete(context, entityOp);

        } else if (entityOp instanceof InsertOrUpdate) {
            totalCount += executeInsertOrUpdate(context, entityOp);
        }
        return totalCount;
    }

    private int executeInsert(Context context, EntityOp entityOp) {
        int totalCount = 0;
        for (CommonRepository repository : this.repository.getOrderedRepositories()) {
            boolean isRoot = repository.isRoot();
            if (isRoot) {
                totalCount += executeRoot(repository, context, entityOp);
                continue;
            }
            boolean isMatch = repository.matches(context);
            boolean isAggregated = repository.isAggregated();
            if (!isMatch && !isAggregated) continue;

            List<?> rootEntities = entityOp.getEntities();
            for (Object rootEntity : rootEntities) {
                List<?> entities = getEntities(repository, rootEntity);
                if (entities == null) continue;

                if (isMatch) {
                    repository.getBoundValue(context, rootEntity, entities);
                }
                Operation operation = new Insert(entities);
                operation.switchRoot(isMatch);
                totalCount += repository.execute(context, operation);
                if (entities.size() == 1) {
                    repository.setBoundId(context, rootEntity, entities.get(0));
                }
            }
        }
        return totalCount;
    }

    private int executeUpdateOrDelete(Context context, EntityOp entityOp) {
        int totalCount = 0;
        for (CommonRepository repository : this.repository.getOrderedRepositories()) {
            boolean isRoot = repository.isRoot();
            if (isRoot) {
                totalCount += executeRoot(repository, context, entityOp);
                continue;
            }
            boolean isMatch = repository.matches(context);
            boolean isAggregated = repository.isAggregated();
            if (!isMatch && !isAggregated) continue;

            List<?> rootEntities = entityOp.getEntities();
            for (Object rootEntity : rootEntities) {
                List<?> entities = getEntities(repository, rootEntity);
                if (entities == null) continue;

                Operation operation = entityOp instanceof Update ? new Update(entities) : new Delete(entities);
                operation.switchRoot(isMatch);
                totalCount += repository.execute(context, operation);
            }
        }
        return totalCount;
    }

    private int executeInsertOrUpdate(Context context, EntityOp entityOp) {
        int totalCount = 0;
        for (CommonRepository repository : this.repository.getOrderedRepositories()) {
            boolean isRoot = repository.isRoot();
            if (isRoot) {
                totalCount += executeRoot(repository, context, entityOp);
                continue;
            }
            boolean isMatch = repository.matches(context);
            boolean isAggregated = repository.isAggregated();
            if (!isMatch && !isAggregated) continue;

            List<?> rootEntities = entityOp.getEntities();
            for (Object rootEntity : rootEntities) {
                List<?> entities = getEntities(repository, rootEntity);
                if (entities == null) continue;

                if (isMatch) {
                    repository.getBoundValue(context, rootEntity, entities);
                }
                OperationFactory operationFactory = repository.getOperationFactory();
                Operation operation = operationFactory.buildInsertOrUpdate(entities);
                operation.switchRoot(isMatch);
                totalCount += repository.execute(context, operation);
                if (entities.size() == 1) {
                    repository.setBoundId(context, rootEntity, entities.get(0));
                }
            }
        }
        return totalCount;
    }

    private int executeRoot(CommonRepository repository, Context context, EntityOp entityOp) {
        if (entityOp.isNotIgnoreRoot()) {
            if (repository.matches(context) || entityOp.isIncludeRoot()) {
                return repository.execute(context, entityOp);
            }
        }
        return 0;
    }

    private List<?> getEntities(CommonRepository repository, Object rootEntity) {
        EntityElement entityElement = repository.getEntityElement();
        Object targetEntity = entityElement.getValue(rootEntity);
        if (targetEntity != null) {
            List<?> entities = CollectionUtils.toList(targetEntity);
            if (!entities.isEmpty()) {
                return entities;
            }
        }
        return null;
    }

}
