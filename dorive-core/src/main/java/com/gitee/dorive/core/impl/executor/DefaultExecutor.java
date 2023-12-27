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
import com.gitee.dorive.core.entity.operation.Operation;
import com.gitee.dorive.core.entity.operation.Query;
import com.gitee.dorive.core.impl.factory.OperationFactory;
import com.gitee.dorive.core.impl.resolver.DerivedResolver;
import com.gitee.dorive.core.repository.AbstractContextRepository;
import com.gitee.dorive.core.repository.CommonRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class DefaultExecutor extends AbstractExecutor implements EntityHandler {

    private final AbstractContextRepository<?, ?> repository;
    private final EntityHandler entityHandler;

    public DefaultExecutor(AbstractContextRepository<?, ?> repository, EntityHandler entityHandler) {
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
        Selector selector = context.getSelector();

        boolean isInsertContext = operation.isInsertContext();
        boolean isIncludeRoot = operation.isIncludeRoot();
        boolean isIgnoreRoot = operation.isIgnoreRoot();

        Object rootEntity = operation.getEntity();
        Assert.notNull(rootEntity, "The root entity cannot be null!");

        DerivedResolver derivedResolver = repository.getDerivedResolver();
        AbstractContextRepository<?, ?> delegateRepository = derivedResolver.deriveRepository(rootEntity);
        delegateRepository = delegateRepository == null ? repository : delegateRepository;

        int totalCount = 0;
        for (CommonRepository repository : delegateRepository.getOrderedRepositories()) {
            boolean isRoot = repository.isRoot();
            if (isIgnoreRoot && isRoot) {
                continue;
            }

            boolean isMatch = selector.matches(context, repository) || (isIncludeRoot && isRoot);
            boolean isAggregated = repository.isAggregated();
            if (!isMatch && !isAggregated) {
                continue;
            }

            PropChain anchorPoint = repository.getAnchorPoint();
            Object targetEntity = isRoot ? rootEntity : anchorPoint.getValue(rootEntity);
            if (targetEntity != null) {
                Collection<?> collection;
                if (targetEntity instanceof Collection) {
                    collection = (Collection<?>) targetEntity;
                } else {
                    collection = Collections.singletonList(targetEntity);
                }
                for (Object entity : collection) {
                    int operationType = Operation.Type.NONE;
                    boolean operable = false;
                    if (isMatch) {
                        operationType = determineType(operation, repository, entity);
                        operable = (operationType & Operation.Type.INSERT_OR_UPDATE_OR_DELETE) != 0;
                        if ((operationType & Operation.Type.INSERT) != 0) {
                            getBoundValue(context, rootEntity, repository, entity);
                        }
                    }
                    if (isAggregated) {
                        OperationFactory operationFactory = repository.getOperationFactory();
                        Operation newOperation = operationFactory.renewOperation(operation, entity);
                        if (newOperation != null) {
                            if (operable) {
                                newOperation.includeRoot();
                            } else {
                                newOperation.ignoreRoot();
                            }
                            totalCount += repository.execute(context, newOperation);
                        }

                    } else if (operable) {
                        if (isRoot && operation.getType() == operationType) {
                            totalCount += repository.execute(context, operation);
                        } else {
                            totalCount += doExecute(context, repository, entity, operationType);
                        }
                    }
                }
                if (isInsertContext && collection.size() == 1) {
                    setBoundId(context, rootEntity, repository, targetEntity);
                }
            }
        }
        return totalCount;
    }

    private int determineType(Operation operation, CommonRepository repository, Object entity) {
        if (operation.isForceInsert()) {
            return Operation.Type.INSERT;
        }
        int type = operation.getType();
        Object primaryKey = repository.getPrimaryKey(entity);
        int operationType = primaryKey == null ? Operation.Type.INSERT : Operation.Type.UPDATE_OR_DELETE;
        return type & operationType;
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

    private int doExecute(Context context, CommonRepository repository, Object entity, int operationType) {
        if (operationType == Operation.Type.INSERT) {
            return repository.insert(context, entity);

        } else if (operationType == Operation.Type.UPDATE) {
            return repository.update(context, entity);

        } else if (operationType == Operation.Type.DELETE) {
            return repository.delete(context, entity);
        }
        return 0;
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
