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
import java.util.concurrent.atomic.AtomicInteger;

@Data
@AllArgsConstructor
public class BatchEntityOpHandler implements EntityOpHandler {

    private final RepositoryContext repositoryContext;

    @Override
    public long handle(Context context, EntityOp entityOp) {
        final AtomicInteger totalCount = new AtomicInteger(0);
        if (entityOp instanceof Insert) {
            execute(context, entityOp, totalCount, (RepositoryItem repositoryItem, boolean isMatch, Object rootEntity, List<?> entities) -> {
                if (isMatch) {
                    repositoryItem.getBoundValue(context, rootEntity, entities);
                }
                Operation operation = new Insert(entities);
                operation.switchRoot(isMatch);
                totalCount.addAndGet(repositoryItem.execute(context, operation));
                if (entities.size() == 1) {
                    repositoryItem.setBoundId(context, rootEntity, entities.get(0));
                }
            });

        } else if (entityOp instanceof Update || entityOp instanceof Delete) {
            execute(context, entityOp, totalCount, (RepositoryItem repositoryItem, boolean isMatch, Object rootEntity, List<?> entities) -> {
                Operation operation = entityOp instanceof Update ? new Update(entities) : new Delete(entities);
                operation.switchRoot(isMatch);
                totalCount.addAndGet(repositoryItem.execute(context, operation));
            });

        } else if (entityOp instanceof InsertOrUpdate) {
            execute(context, entityOp, totalCount, (RepositoryItem repositoryItem, boolean isMatch, Object rootEntity, List<?> entities) -> {
                if (isMatch) {
                    repositoryItem.getBoundValue(context, rootEntity, entities);
                }
                OperationFactory operationFactory = repositoryItem.getOperationFactory();
                Operation operation = operationFactory.buildInsertOrUpdate(entities);
                operation.switchRoot(isMatch);
                totalCount.addAndGet(repositoryItem.execute(context, operation));
                if (entities.size() == 1) {
                    repositoryItem.setBoundId(context, rootEntity, entities.get(0));
                }
            });
        }
        return totalCount.get();
    }

    private void execute(Context context, EntityOp entityOp, AtomicInteger totalCount, Executor executor) {
        for (RepositoryItem repositoryItem : repositoryContext.getOrderedRepositories()) {
            if (repositoryItem.isRoot()) {
                if (entityOp.isNotIgnoreRoot()) {
                    boolean isMatch = repositoryContext.matches(context, repositoryItem);
                    if (isMatch || entityOp.isIncludeRoot()) {
                        totalCount.addAndGet(repositoryItem.execute(context, entityOp));
                    }
                }
            } else {
                boolean isMatch = repositoryContext.matches(context, repositoryItem);
                if (isMatch || repositoryItem.isAggregated()) {
                    List<?> rootEntities = entityOp.getEntities();
                    for (Object rootEntity : rootEntities) {
                        List<?> entities = getEntities(repositoryItem, rootEntity);
                        if (entities != null) {
                            executor.execute(repositoryItem, isMatch, rootEntity, entities);
                        }
                    }
                }
            }
        }
    }

    private List<?> getEntities(RepositoryItem repositoryItem, Object rootEntity) {
        EntityElement entityElement = repositoryItem.getEntityElement();
        Object targetEntity = entityElement.getValue(rootEntity);
        if (targetEntity != null) {
            List<?> entities = CollectionUtils.toList(targetEntity);
            if (!entities.isEmpty()) {
                return entities;
            }
        }
        return null;
    }

    private interface Executor {
        void execute(RepositoryItem repositoryItem, boolean isMatch, Object rootEntity, List<?> entities);
    }
}
