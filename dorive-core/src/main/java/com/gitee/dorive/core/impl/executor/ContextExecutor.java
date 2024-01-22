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
import com.gitee.dorive.api.entity.element.PropChain;
import com.gitee.dorive.core.api.binder.Binder;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.context.Selector;
import com.gitee.dorive.core.api.executor.EntityHandler;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.entity.operation.Delete;
import com.gitee.dorive.core.entity.operation.Insert;
import com.gitee.dorive.core.entity.operation.InsertOrUpdate;
import com.gitee.dorive.core.entity.operation.Operation;
import com.gitee.dorive.core.entity.operation.Query;
import com.gitee.dorive.core.entity.operation.Update;
import com.gitee.dorive.core.impl.factory.OperationFactory;
import com.gitee.dorive.core.impl.resolver.DerivedResolver;
import com.gitee.dorive.core.repository.AbstractContextRepository;
import com.gitee.dorive.core.repository.CommonRepository;
import com.gitee.dorive.core.util.CollectionUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collection;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class ContextExecutor extends AbstractExecutor implements EntityHandler {

    private final AbstractContextRepository<?, ?> repository;
    private final EntityHandler entityHandler;

    public ContextExecutor(AbstractContextRepository<?, ?> repository, EntityHandler entityHandler) {
        this.repository = repository;
        this.entityHandler = entityHandler;
    }

    @Override
    public Result<Object> executeQuery(Context context, Query query) {
        Assert.isTrue(!query.isEmpty(), "The query cannot be empty!");
        Selector selector = context.getSelector();
        CommonRepository rootRepository = repository.getRootRepository();
        if (selector.matches(context, rootRepository) || query.isIncludeRoot()) {
            Result<Object> result = rootRepository.executeQuery(context, query);
            List<Object> entities = result.getRecords();
            if (!entities.isEmpty()) {
                handle(context, entities);
            }
            return result;
        }
        return new Result<>();
    }

    @Override
    public long handle(Context context, List<Object> entities) {
        return entityHandler.handle(context, entities);
    }

    @Override
    public long executeCount(Context context, Query query) {
        throw new RuntimeException("This method does not support!");
    }

    @Override
    public int execute(Context context, Operation operation) {
        Object rootEntity = operation.getEntity();
        Assert.notNull(rootEntity, "The root entity cannot be null!");

        DerivedResolver derivedResolver = repository.getDerivedResolver();
        AbstractContextRepository<?, ?> delegateRepository = derivedResolver.deriveRepository(rootEntity);
        delegateRepository = delegateRepository == null ? repository : delegateRepository;

        if (operation instanceof Insert) {
            return executeInsert(context, operation, delegateRepository);

        } else if (operation instanceof Update || operation instanceof Delete) {
            return executeUpdateOrDelete(context, operation, delegateRepository);

        } else if (operation instanceof InsertOrUpdate) {
            return executeInsertOrUpdate(context, operation, delegateRepository);
        }
        return 0;
    }

    private int executeInsert(Context context, Operation operation, AbstractContextRepository<?, ?> delegateRepository) {
        Selector selector = context.getSelector();
        Object rootEntity = operation.getEntity();
        int totalCount = 0;
        for (CommonRepository repository : delegateRepository.getOrderedRepositories()) {
            if (repository.isRoot()) {
                if (!operation.isIgnoreRoot()) {
                    if (selector.matches(context, repository) || operation.isIncludeRoot()) {
                        getBoundValue(context, rootEntity, repository, rootEntity);
                        totalCount += repository.execute(context, operation);
                        setBoundId(context, rootEntity, repository, rootEntity);
                    }
                }
            } else {
                boolean isMatch = selector.matches(context, repository);
                boolean isAggregated = repository.isAggregated();
                if (!isMatch && !isAggregated) {
                    continue;
                }
                PropChain anchorPoint = repository.getAnchorPoint();
                Object targetEntity = anchorPoint.getValue(rootEntity);
                if (targetEntity == null) {
                    continue;
                }
                OperationFactory operationFactory = repository.getOperationFactory();
                Collection<?> collection = CollectionUtils.toCollection(targetEntity);
                for (Object entity : collection) {
                    if (isMatch) {
                        getBoundValue(context, rootEntity, repository, entity);
                    }
                    Operation newOperation = operationFactory.buildInsert(entity);
                    newOperation.switchRoot(isMatch);
                    totalCount += repository.execute(context, newOperation);
                }
                if (collection.size() == 1) {
                    setBoundId(context, rootEntity, repository, collection.iterator().next());
                }
            }
        }
        return totalCount;
    }

    private int executeUpdateOrDelete(Context context, Operation operation, AbstractContextRepository<?, ?> delegateRepository) {
        Selector selector = context.getSelector();
        Object rootEntity = operation.getEntity();
        int totalCount = 0;
        if (!operation.isIgnoreRoot()) {
            CommonRepository rootRepository = delegateRepository.getRootRepository();
            if (selector.matches(context, rootRepository) || operation.isIncludeRoot()) {
                Object primaryKey = rootRepository.getPrimaryKey(rootEntity);
                if (primaryKey != null) {
                    totalCount += rootRepository.execute(context, operation);
                }
            }
        }
        for (CommonRepository subRepository : delegateRepository.getSubRepositories()) {
            boolean isMatch = selector.matches(context, subRepository);
            boolean isAggregated = subRepository.isAggregated();
            if (!isMatch && !isAggregated) {
                continue;
            }
            PropChain anchorPoint = subRepository.getAnchorPoint();
            Object targetEntity = anchorPoint.getValue(rootEntity);
            if (targetEntity == null) {
                continue;
            }
            OperationFactory operationFactory = subRepository.getOperationFactory();
            Collection<?> collection = CollectionUtils.toCollection(targetEntity);
            for (Object entity : collection) {
                Object primaryKey = subRepository.getPrimaryKey(entity);
                Operation newOperation = null;
                if ((isMatch && primaryKey != null) || isAggregated) {
                    newOperation = operation instanceof Update ? operationFactory.buildUpdate(entity) : operationFactory.buildDelete(entity);
                }
                if (newOperation != null) {
                    newOperation.switchRoot(isMatch);
                    totalCount += subRepository.execute(context, newOperation);
                }
            }
        }
        return totalCount;
    }

    private int executeInsertOrUpdate(Context context, Operation operation, AbstractContextRepository<?, ?> delegateRepository) {
        Selector selector = context.getSelector();
        Object rootEntity = operation.getEntity();
        int totalCount = 0;
        for (CommonRepository repository : delegateRepository.getOrderedRepositories()) {
            OperationFactory operationFactory = repository.getOperationFactory();
            if (repository.isRoot()) {
                if (!operation.isIgnoreRoot()) {
                    if (selector.matches(context, repository) || operation.isIncludeRoot()) {
                        Operation newOperation = operationFactory.buildInsertOrUpdate(rootEntity);
                        if (newOperation instanceof Insert) {
                            getBoundValue(context, rootEntity, repository, rootEntity);
                            totalCount += repository.execute(context, newOperation);
                            setBoundId(context, rootEntity, repository, rootEntity);
                        } else {
                            totalCount += repository.execute(context, newOperation);
                        }
                    }
                }
            } else {
                boolean isMatch = selector.matches(context, repository);
                boolean isAggregated = repository.isAggregated();
                if (!isMatch && !isAggregated) {
                    continue;
                }
                PropChain anchorPoint = repository.getAnchorPoint();
                Object targetEntity = anchorPoint.getValue(rootEntity);
                if (targetEntity == null) {
                    continue;
                }
                Collection<?> collection = CollectionUtils.toCollection(targetEntity);
                Object onlyOne = collection.size() == 1 ? collection.iterator().next() : null;
                boolean isOnlyOneInsert = onlyOne != null && repository.getPrimaryKey(onlyOne) == null;
                for (Object entity : collection) {
                    Object primaryKey = repository.getPrimaryKey(entity);
                    if (isMatch && primaryKey == null) {
                        getBoundValue(context, rootEntity, repository, entity);
                    }
                    Operation newOperation;
                    if (isAggregated) {
                        newOperation = new InsertOrUpdate(entity);
                    } else if (primaryKey == null) {
                        newOperation = operationFactory.buildInsert(entity);
                    } else {
                        newOperation = operationFactory.buildUpdate(entity);
                    }
                    newOperation.switchRoot(isMatch);
                    totalCount += repository.execute(context, newOperation);
                }
                if (isOnlyOneInsert) {
                    setBoundId(context, rootEntity, repository, onlyOne);
                }
            }
        }
        return totalCount;
    }

    private void getBoundValue(Context context, Object rootEntity, CommonRepository repository, Object entity) {
        for (Binder binder : repository.getBinderResolver().getBoundValueBinders()) {
            Object fieldValue = binder.getFieldValue(context, entity);
            if (fieldValue == null) {
                Object boundValue = binder.getBoundValue(context, rootEntity);
                if (boundValue != null) {
                    binder.setFieldValue(context, entity, boundValue);
                }
            }
        }
    }

    private void setBoundId(Context context, Object rootEntity, CommonRepository repository, Object entity) {
        Binder binder = repository.getBinderResolver().getBoundIdBinder();
        if (binder != null) {
            Object boundValue = binder.getBoundValue(context, rootEntity);
            if (boundValue == null) {
                Object primaryKey = binder.getFieldValue(context, entity);
                if (primaryKey != null) {
                    binder.setBoundValue(context, rootEntity, primaryKey);
                }
            }
        }
    }

}
