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
import com.gitee.dorive.core.api.Binder;
import com.gitee.dorive.core.api.EntityHandler;
import com.gitee.dorive.core.api.Context;
import com.gitee.dorive.core.api.Selector;
import com.gitee.dorive.api.entity.element.PropChain;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.entity.operation.Operation;
import com.gitee.dorive.core.entity.operation.Query;
import com.gitee.dorive.core.impl.OperationTypeResolver;
import com.gitee.dorive.core.impl.resolver.DelegateResolver;
import com.gitee.dorive.core.repository.AbstractContextRepository;
import com.gitee.dorive.core.repository.CommonRepository;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@ToString
public class ChainExecutor extends AbstractExecutor implements EntityHandler {

    private final AbstractContextRepository<?, ?> repository;
    private final EntityHandler entityHandler;

    public ChainExecutor(AbstractContextRepository<?, ?> repository, EntityHandler entityHandler) {
        this.repository = repository;
        this.entityHandler = entityHandler;
    }

    @Override
    public Result<Object> executeQuery(Context context, Query query) {
        Assert.isTrue(query.getPrimaryKey() != null || query.getExample() != null,
                "The query criteria cannot be null!");

        CommonRepository rootRepository = repository.getRootRepository();
        boolean isIncludeRoot = (query.getType() & Operation.INCLUDE_ROOT) == Operation.INCLUDE_ROOT;

        Selector selector = context.getSelector();

        if (selector.matches(context, rootRepository) || isIncludeRoot) {
            int totalCount = 0;

            Result<Object> result = rootRepository.executeQuery(context, query);
            totalCount += result.getTotal();

            List<Object> rootEntities = result.getRecords();
            if (!rootEntities.isEmpty()) {
                totalCount += handleEntities(context, rootEntities);
            }

            result.setTotal(totalCount);
            return result;
        }

        return new Result<>();
    }

    @Override
    public int handleEntities(Context context, List<Object> rootEntities) {
        return entityHandler.handleEntities(context, rootEntities);
    }

    @Override
    public int execute(Context context, Operation operation) {
        int expectedOperationType = operation.getType();

        boolean isInsertContext = (expectedOperationType & Operation.INSERT) == Operation.INSERT;
        boolean isIgnoreRoot = (expectedOperationType & Operation.IGNORE_ROOT) == Operation.IGNORE_ROOT;
        boolean isIncludeRoot = (expectedOperationType & Operation.INCLUDE_ROOT) == Operation.INCLUDE_ROOT;

        int ignoreRootOperationType = expectedOperationType | Operation.IGNORE_ROOT;
        int includeRootOperationType = expectedOperationType | Operation.INCLUDE_ROOT;

        Object rootEntity = operation.getEntity();
        Assert.notNull(rootEntity, "The rootEntity cannot be null!");

        DelegateResolver delegateResolver = repository.getDelegateResolver();
        AbstractContextRepository<?, ?> delegateRepository = delegateResolver.delegateRepository(rootEntity);
        delegateRepository = delegateRepository == null ? repository : delegateRepository;

        Selector selector = context.getSelector();

        int totalCount = 0;
        for (CommonRepository repository : delegateRepository.getOrderedRepositories()) {

            if (isIgnoreRoot && repository.isRoot()) {
                continue;
            }

            boolean isMatch = selector.matches(context, repository);
            boolean isForceInclude = isIncludeRoot && repository.isRoot();
            boolean isAggregated = repository.isAggregated();

            if (!isMatch && !isForceInclude && !isAggregated) {
                continue;
            }

            PropChain anchorPoint = repository.getAnchorPoint();
            Object targetEntity = anchorPoint == null ? rootEntity : anchorPoint.getValue(rootEntity);
            if (targetEntity != null) {

                Collection<?> collection;
                Object boundIdEntity = null;
                if (targetEntity instanceof Collection) {
                    collection = (Collection<?>) targetEntity;

                } else {
                    collection = Collections.singletonList(targetEntity);
                    boundIdEntity = targetEntity;
                }

                if (isMatch || isForceInclude) {
                    int contextOperationType = OperationTypeResolver.resolveOperationType(context, repository);

                    for (Object entity : collection) {
                        Object primaryKey = repository.getPrimaryKey(entity);
                        int operationType = OperationTypeResolver.mergeOperationType(expectedOperationType, contextOperationType, primaryKey);

                        if ((operationType & Operation.INSERT) == Operation.INSERT) {
                            getBoundValueFromContext(context, rootEntity, repository, entity);
                        }

                        if (isAggregated) {
                            operationType = (operationType & Operation.INSERT_OR_UPDATE_OR_DELETE) > 0 ? includeRootOperationType : ignoreRootOperationType;
                            Operation newOperation = new Operation(operationType, entity);
                            totalCount += repository.execute(context, newOperation);

                        } else {
                            totalCount += doExecute(context, repository, entity, operationType);
                        }
                    }

                    if (isInsertContext && boundIdEntity != null) {
                        setBoundIdForBoundEntity(context, rootEntity, repository, boundIdEntity);
                    }

                } else if (isAggregated) {
                    for (Object entity : collection) {
                        Operation newOperation = new Operation(ignoreRootOperationType, entity);
                        totalCount += repository.execute(context, newOperation);
                    }
                }
            }
        }
        return totalCount;
    }

    private int doExecute(Context context, CommonRepository repository, Object entity, int operationType) {
        if (operationType == Operation.INSERT) {
            return repository.insert(context, entity);

        } else if (operationType == Operation.UPDATE) {
            return repository.update(context, entity);

        } else if (operationType == Operation.INSERT_OR_UPDATE) {
            return repository.insertOrUpdate(context, entity);

        } else if (operationType == Operation.DELETE) {
            return repository.delete(context, entity);
        }
        return 0;
    }

    private void getBoundValueFromContext(Context context, Object rootEntity, CommonRepository repository, Object entity) {
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

    private void setBoundIdForBoundEntity(Context context, Object rootEntity, CommonRepository repository, Object entity) {
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
