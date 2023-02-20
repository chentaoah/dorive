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
import com.gitee.dorive.core.api.Observed;
import com.gitee.dorive.core.entity.BoundedContext;
import com.gitee.dorive.core.entity.element.PropertyChain;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.entity.operation.Operation;
import com.gitee.dorive.core.entity.operation.Query;
import com.gitee.dorive.core.impl.OperationTypeResolver;
import com.gitee.dorive.core.impl.observe.ObservedResult;
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
    public Result<Object> executeQuery(BoundedContext boundedContext, Query query) {
        Assert.isTrue(query.getPrimaryKey() != null || query.getExample() != null,
                "The query criteria cannot be null!");

        CommonRepository rootRepository = repository.getRootRepository();
        boolean isIncludeRoot = (query.getType() & Operation.INCLUDE_ROOT) == Operation.INCLUDE_ROOT;

        if (boundedContext.isMatch(rootRepository) || isIncludeRoot) {
            Result<Object> result = rootRepository.executeQuery(boundedContext, query);
            List<Object> rootEntities = result.getRecords();
            if (!rootEntities.isEmpty()) {
                handleEntities(boundedContext, rootEntities);
            }
            return result;
        }

        return new Result<>();
    }

    @Override
    public void handleEntities(BoundedContext boundedContext, List<Object> rootEntities) {
        entityHandler.handleEntities(boundedContext, rootEntities);
    }

    @Override
    public int execute(BoundedContext boundedContext, Operation operation) {
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

        int totalCount = 0;
        for (CommonRepository repository : delegateRepository.getOrderedRepositories()) {

            if (isIgnoreRoot && repository.isRoot()) {
                continue;
            }

            boolean isObserver = boundedContext.isObserver();
            boolean isMatch = boundedContext.isMatch(repository);
            boolean isForceInclude = isIncludeRoot && repository.isRoot();
            boolean isAggregated = repository.isAggregated();

            if (!isObserver && !isMatch && !isForceInclude && !isAggregated) {
                continue;
            }

            PropertyChain anchorPoint = repository.getAnchorPoint();
            Object targetEntity = anchorPoint == null ? rootEntity : anchorPoint.getValue(rootEntity);
            if (targetEntity != null) {

                int observedOperationType = Operation.INSERT_OR_UPDATE_OR_DELETE;
                if (isObserver) {
                    if (targetEntity instanceof Observed) {
                        ObservedResult result = ((Observed) targetEntity).accept(repository, boundedContext, targetEntity);
                        observedOperationType = result.getOperationType();
                        totalCount += result.getTotalCount();
                    }
                }

                Collection<?> collection;
                Object boundIdEntity = null;
                if (targetEntity instanceof Collection) {
                    collection = (Collection<?>) targetEntity;

                } else {
                    collection = Collections.singletonList(targetEntity);
                    boundIdEntity = targetEntity;
                }

                if (isMatch || isForceInclude) {
                    int contextOperationType = OperationTypeResolver.resolveOperationType(boundedContext, repository);

                    for (Object entity : collection) {
                        Object primaryKey = repository.getPrimaryKey(entity);
                        int operationType = OperationTypeResolver.mergeOperationType(expectedOperationType, contextOperationType, primaryKey);

                        if ((operationType & Operation.INSERT) == Operation.INSERT) {
                            getBoundValueFromContext(boundedContext, rootEntity, repository, entity);
                        }

                        operationType = operationType & observedOperationType;

                        if (isAggregated) {
                            operationType = (operationType & Operation.INSERT_OR_UPDATE_OR_DELETE) > 0 ? includeRootOperationType : ignoreRootOperationType;
                            Operation newOperation = new Operation(operationType, entity);
                            totalCount += repository.execute(boundedContext, newOperation);

                        } else {
                            totalCount += doExecute(boundedContext, repository, entity, operationType);
                        }
                    }

                    if (isInsertContext && boundIdEntity != null) {
                        setBoundIdForBoundEntity(boundedContext, rootEntity, repository, boundIdEntity);
                    }

                } else if (isAggregated) {
                    for (Object entity : collection) {
                        Operation newOperation = new Operation(ignoreRootOperationType, entity);
                        totalCount += repository.execute(boundedContext, newOperation);
                    }
                }
            }
        }
        return totalCount;
    }

    private int doExecute(BoundedContext boundedContext, CommonRepository repository, Object entity, int operationType) {
        if (operationType == Operation.INSERT) {
            return repository.insert(boundedContext, entity);

        } else if (operationType == Operation.UPDATE) {
            return repository.update(boundedContext, entity);

        } else if (operationType == Operation.INSERT_OR_UPDATE) {
            return repository.insertOrUpdate(boundedContext, entity);

        } else if (operationType == Operation.DELETE) {
            return repository.delete(boundedContext, entity);
        }
        return 0;
    }

    private void getBoundValueFromContext(BoundedContext boundedContext, Object rootEntity, CommonRepository repository, Object entity) {
        for (Binder binder : repository.getBinderResolver().getBoundValueBinders()) {
            Object fieldValue = binder.getFieldValue(boundedContext, entity);
            if (fieldValue == null) {
                Object boundValue = binder.getBoundValue(boundedContext, rootEntity);
                if (boundValue != null) {
                    binder.setFieldValue(boundedContext, entity, boundValue);
                }
            }
        }
    }

    private void setBoundIdForBoundEntity(BoundedContext boundedContext, Object rootEntity, CommonRepository repository, Object entity) {
        Binder binder = repository.getBinderResolver().getBoundIdBinder();
        if (binder != null) {
            Object boundValue = binder.getBoundValue(boundedContext, rootEntity);
            if (boundValue == null) {
                Object primaryKey = binder.getFieldValue(boundedContext, entity);
                if (primaryKey != null) {
                    binder.setBoundValue(boundedContext, rootEntity, primaryKey);
                }
            }
        }
    }

}
