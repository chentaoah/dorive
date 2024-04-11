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

package com.gitee.dorive.core.impl.executor;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import com.gitee.dorive.api.entity.PropChain;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.executor.EntityHandler;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.entity.operation.EntityOp;
import com.gitee.dorive.core.entity.operation.Operation;
import com.gitee.dorive.core.entity.operation.cop.Query;
import com.gitee.dorive.core.entity.operation.eop.Delete;
import com.gitee.dorive.core.entity.operation.eop.Insert;
import com.gitee.dorive.core.entity.operation.eop.InsertOrUpdate;
import com.gitee.dorive.core.entity.operation.eop.Update;
import com.gitee.dorive.core.impl.resolver.DerivedResolver;
import com.gitee.dorive.core.repository.AbstractContextRepository;
import com.gitee.dorive.core.repository.CommonRepository;
import com.gitee.dorive.core.util.CollectionUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ContextExecutor extends AbstractExecutor {

    private final AbstractContextRepository<?, ?> repository;
    private final EntityHandler entityHandler;

    public ContextExecutor(AbstractContextRepository<?, ?> repository, EntityHandler entityHandler) {
        this.repository = repository;
        this.entityHandler = entityHandler;
    }

    @Override
    public Result<Object> executeQuery(Context context, Query query) {
        Assert.isTrue(!query.isEmpty(), "The query cannot be empty!");
        CommonRepository rootRepository = repository.getRootRepository();
        if (rootRepository.matches(context) || query.isIncludeRoot()) {
            Result<Object> result = rootRepository.executeQuery(context, query);
            List<Object> entities = result.getRecords();
            if (!entities.isEmpty()) {
                populate(context, entities);
            }
            return result;
        }
        return Result.emptyResult(query);
    }

    public void populate(Context context, List<Object> entities) {
        DerivedResolver derivedResolver = repository.getDerivedResolver();
        if (!derivedResolver.hasDerived()) {
            entityHandler.handle(context, entities);
        } else {
            for (Pair<AbstractContextRepository<?, ?>, List<Object>> pair : derivedResolver.distribute(entities)) {
                if (pair.getKey() == this.repository) { // 避免自循环
                    entityHandler.handle(context, pair.getValue());
                } else {
                    ContextExecutor contextExecutor = (ContextExecutor) pair.getKey().getExecutor();
                    contextExecutor.populate(context, pair.getValue());
                }
            }
        }
    }

    @Override
    public long executeCount(Context context, Query query) {
        throw new RuntimeException("This method does not support!");
    }

    @Override
    public int execute(Context context, Operation operation) {
        EntityOp entityOp = (EntityOp) operation;
        List<?> entities = entityOp.getEntities();
        Assert.notEmpty(entities, "The entities cannot be empty!");

        int totalCount = 0;
        for (Pair<AbstractContextRepository<?, ?>, List<Object>> pair : repository.getDerivedResolver().distribute(entities)) {
            ContextExecutor contextExecutor = (ContextExecutor) pair.getKey().getExecutor();
            if (operation instanceof Insert) {
                Insert insert = new Insert(pair.getValue());
                totalCount += contextExecutor.executeInsert(context, insert);

            } else if (operation instanceof Update) {
                Update update = new Update(pair.getValue());
                totalCount += contextExecutor.executeUpdateOrDelete(context, update);

            } else if (operation instanceof Delete) {
                Delete delete = new Delete(pair.getValue());
                totalCount += contextExecutor.executeUpdateOrDelete(context, delete);

            } else if (operation instanceof InsertOrUpdate) {
                InsertOrUpdate insertOrUpdate = new InsertOrUpdate(pair.getValue());
                totalCount += contextExecutor.executeInsertOrUpdate(context, insertOrUpdate);
            }
        }
        return totalCount;
    }

    public int executeInsert(Context context, Insert insert) {
        int totalCount = 0;
        for (CommonRepository repository : this.repository.getOrderedRepositories()) {
            if (repository.isRoot()) {
                if (!insert.isIgnoreRoot()) {
                    if (repository.matches(context) || insert.isIncludeRoot()) {
                        totalCount += repository.execute(context, insert);
                    }
                }
            } else {
                boolean isMatch = repository.matches(context);
                boolean isAggregated = repository.isAggregated();
                if (!isMatch && !isAggregated) {
                    continue;
                }
                List<?> rootEntities = insert.getEntities();
                for (Object rootEntity : rootEntities) {
                    PropChain anchorPoint = repository.getAnchorPoint();
                    Object targetEntity = anchorPoint.getValue(rootEntity);
                    if (targetEntity == null) {
                        continue;
                    }
                    List<?> entities = CollectionUtils.toList(targetEntity);
                    if (isMatch) {
                        repository.getBoundValue(context, rootEntity, entities);
                    }
                    Operation operation = new Insert(entities);
                    operation.switchRoot(isMatch);
                    totalCount += repository.execute(context, operation);
                    if (entities.size() == 1) {
                        repository.setBoundId(context, rootEntity, entities.iterator().next());
                    }
                }
            }
        }
        return totalCount;
    }

    public int executeUpdateOrDelete(Context context, EntityOp entityOp) {
        int totalCount = 0;
        if (!entityOp.isIgnoreRoot()) {
            CommonRepository repository = this.repository.getRootRepository();
            if (repository.matches(context) || entityOp.isIncludeRoot()) {
                totalCount += repository.execute(context, entityOp);
            }
        }
        for (CommonRepository repository : this.repository.getSubRepositories()) {
            boolean isMatch = repository.matches(context);
            boolean isAggregated = repository.isAggregated();
            if (!isMatch && !isAggregated) {
                continue;
            }
            List<?> rootEntities = entityOp.getEntities();
            for (Object rootEntity : rootEntities) {
                PropChain anchorPoint = repository.getAnchorPoint();
                Object targetEntity = anchorPoint.getValue(rootEntity);
                if (targetEntity == null) {
                    continue;
                }
                List<?> entities = CollectionUtils.toList(targetEntity);
                Operation operation = entityOp instanceof Update ? new Update(entities) : new Delete(entities);
                operation.switchRoot(isMatch);
                totalCount += repository.execute(context, operation);
            }
        }
        return totalCount;
    }

    public int executeInsertOrUpdate(Context context, InsertOrUpdate insertOrUpdate) {
        int totalCount = 0;
        for (CommonRepository repository : this.repository.getOrderedRepositories()) {
            if (repository.isRoot()) {
                if (!insertOrUpdate.isIgnoreRoot()) {
                    if (repository.matches(context) || insertOrUpdate.isIncludeRoot()) {
                        totalCount += repository.execute(context, insertOrUpdate);
                    }
                }
            } else {
                boolean isMatch = repository.matches(context);
                boolean isAggregated = repository.isAggregated();
                if (!isMatch && !isAggregated) {
                    continue;
                }
                List<?> rootEntities = insertOrUpdate.getEntities();
                for (Object rootEntity : rootEntities) {
                    PropChain anchorPoint = repository.getAnchorPoint();
                    Object targetEntity = anchorPoint.getValue(rootEntity);
                    if (targetEntity == null) {
                        continue;
                    }
                    List<?> entities = CollectionUtils.toList(targetEntity);
                    if (isMatch) {
                        repository.getBoundValue(context, rootEntity, entities);
                    }
                    Operation operation = new InsertOrUpdate(entities);
                    operation.switchRoot(isMatch);
                    totalCount += repository.execute(context, operation);
                    if (entities.size() == 1) {
                        repository.setBoundId(context, rootEntity, entities.iterator().next());
                    }
                }
            }
        }
        return totalCount;
    }

}
