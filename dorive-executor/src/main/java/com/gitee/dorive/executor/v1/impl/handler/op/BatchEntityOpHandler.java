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

package com.gitee.dorive.executor.v1.impl.handler.op;

import com.gitee.dorive.base.v1.common.entity.EntityElement;
import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.base.v1.core.entity.eop.Delete;
import com.gitee.dorive.base.v1.core.entity.eop.Insert;
import com.gitee.dorive.base.v1.core.entity.eop.InsertOrUpdate;
import com.gitee.dorive.base.v1.core.entity.eop.Update;
import com.gitee.dorive.base.v1.core.entity.op.EntityOp;
import com.gitee.dorive.base.v1.core.entity.op.Operation;
import com.gitee.dorive.base.v1.core.impl.OperationFactory;
import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import com.gitee.dorive.executor.v1.util.CollectionUtils;
import com.gitee.dorive.base.v1.executor.api.EntityOpHandler;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BatchEntityOpHandler implements EntityOpHandler {

    private final RepositoryContext repository;

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
        for (RepositoryItem repository : this.repository.getOrderedRepositories()) {
            boolean isRoot = repository.isRoot();
            if (isRoot) {
                totalCount += executeRoot(repository, context, entityOp);
                continue;
            }
            boolean isMatch = this.repository.matches(context, repository);
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
        for (RepositoryItem repository : this.repository.getOrderedRepositories()) {
            boolean isRoot = repository.isRoot();
            if (isRoot) {
                totalCount += executeRoot(repository, context, entityOp);
                continue;
            }
            boolean isMatch = this.repository.matches(context, repository);
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
        for (RepositoryItem repository : this.repository.getOrderedRepositories()) {
            boolean isRoot = repository.isRoot();
            if (isRoot) {
                totalCount += executeRoot(repository, context, entityOp);
                continue;
            }
            boolean isMatch = this.repository.matches(context, repository);
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

    private int executeRoot(RepositoryItem repository, Context context, EntityOp entityOp) {
        if (entityOp.isNotIgnoreRoot()) {
            if (this.repository.matches(context, repository) || entityOp.isIncludeRoot()) {
                return repository.execute(context, entityOp);
            }
        }
        return 0;
    }

    private List<?> getEntities(RepositoryItem repository, Object rootEntity) {
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
