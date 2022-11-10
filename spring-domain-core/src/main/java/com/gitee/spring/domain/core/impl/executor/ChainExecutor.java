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
package com.gitee.spring.domain.core.impl.executor;

import cn.hutool.core.lang.Assert;
import com.gitee.spring.domain.core.api.Binder;
import com.gitee.spring.domain.core.api.EntityHandler;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.PropertyChain;
import com.gitee.spring.domain.core.entity.executor.Page;
import com.gitee.spring.domain.core.entity.executor.Result;
import com.gitee.spring.domain.core.entity.operation.Operation;
import com.gitee.spring.domain.core.entity.operation.Query;
import com.gitee.spring.domain.core.impl.OperationTypeResolver;
import com.gitee.spring.domain.core.impl.resolver.DelegateResolver;
import com.gitee.spring.domain.core.repository.AbstractContextRepository;
import com.gitee.spring.domain.core.repository.ConfiguredRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class ChainExecutor extends AbstractExecutor implements EntityHandler {

    private final AbstractContextRepository<?, ?> repository;
    private final EntityHandler entityHandler;

    private final OperationTypeResolver operationTypeResolver = new OperationTypeResolver();

    public ChainExecutor(AbstractContextRepository<?, ?> repository, EntityHandler entityHandler) {
        this.repository = repository;
        this.entityHandler = entityHandler;
    }

    @Override
    public Result executeQuery(BoundedContext boundedContext, Query query) {
        ConfiguredRepository rootRepository = repository.getRootRepository();
        if (query.getPrimaryKey() != null) {
            Object rootEntity = rootRepository.selectByPrimaryKey(boundedContext, query.getPrimaryKey());
            if (rootEntity != null) {
                handleEntities(boundedContext, Collections.singletonList(rootEntity));
            }
            return new Result(rootEntity);

        } else if (query.withoutPage()) {
            List<Object> rootEntities = rootRepository.selectByExample(boundedContext, query.getExample());
            if (!rootEntities.isEmpty()) {
                handleEntities(boundedContext, rootEntities);
            }
            return new Result(rootEntities);

        } else {
            Page<Object> page = rootRepository.selectPageByExample(boundedContext, query.getExample());
            List<Object> rootEntities = page.getRecords();
            if (!rootEntities.isEmpty()) {
                handleEntities(boundedContext, rootEntities);
            }
            return new Result(page);
        }
    }

    @Override
    public void handleEntities(BoundedContext boundedContext, List<Object> rootEntities) {
        doHandleEntities(boundedContext, rootEntities);
    }

    protected void doHandleEntities(BoundedContext boundedContext, List<Object> rootEntities) {
        entityHandler.handleEntities(boundedContext, rootEntities);
    }

    @Override
    public Operation buildInsertOrUpdate(BoundedContext boundedContext, Object entity) {
        return new Operation(Operation.INSERT_OR_UPDATE, entity);
    }

    @Override
    public int execute(BoundedContext boundedContext, Operation operation) {
        int expectedOperationType = operation.getType();
        boolean isInsertContext = (expectedOperationType & Operation.INSERT) == Operation.INSERT;

        Object rootEntity = operation.getEntity();
        Assert.notNull(rootEntity, "The rootEntity cannot be null!");
        
        DelegateResolver delegateResolver = repository.getDelegateResolver();
        AbstractContextRepository<?, ?> delegateRepository = delegateResolver.delegateRepository(rootEntity);

        int totalCount = 0;
        for (ConfiguredRepository orderedRepository : delegateRepository.getOrderedRepositories()) {
            PropertyChain anchorPoint = orderedRepository.getAnchorPoint();
            Object targetEntity = anchorPoint == null ? rootEntity : anchorPoint.getValue(rootEntity);
            if (targetEntity != null && orderedRepository.matchContext(boundedContext)) {
                int contextOperationType = operationTypeResolver.resolveOperationType(boundedContext, orderedRepository);

                if (targetEntity instanceof Collection) {
                    for (Object eachEntity : (Collection<?>) targetEntity) {
                        int operationType = operationTypeResolver.mergeOperationType(expectedOperationType, contextOperationType, eachEntity);
                        if ((operationType & Operation.INSERT) == Operation.INSERT) {
                            getBoundValueFromContext(boundedContext, rootEntity, orderedRepository, eachEntity);
                        }
                        operationType = orderedRepository.isAggregated() ? expectedOperationType : operationType;
                        totalCount += doExecute(boundedContext, orderedRepository, eachEntity, operationType);
                    }
                } else {
                    int operationType = operationTypeResolver.mergeOperationType(expectedOperationType, contextOperationType, targetEntity);
                    if ((operationType & Operation.INSERT) == Operation.INSERT) {
                        getBoundValueFromContext(boundedContext, rootEntity, orderedRepository, targetEntity);
                    }
                    operationType = orderedRepository.isAggregated() ? expectedOperationType : operationType;
                    totalCount += doExecute(boundedContext, orderedRepository, targetEntity, operationType);

                    if (isInsertContext) {
                        setBoundIdForBoundEntity(boundedContext, rootEntity, orderedRepository, targetEntity);
                    }
                }
            }
        }
        return totalCount;
    }

    private int doExecute(BoundedContext boundedContext, ConfiguredRepository repository, Object entity, int operationType) {
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

    private void getBoundValueFromContext(BoundedContext boundedContext, Object rootEntity, ConfiguredRepository repository, Object entity) {
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

    private void setBoundIdForBoundEntity(BoundedContext boundedContext, Object rootEntity, ConfiguredRepository repository, Object entity) {
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
