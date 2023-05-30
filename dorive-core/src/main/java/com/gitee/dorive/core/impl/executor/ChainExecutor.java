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
import com.gitee.dorive.api.constant.OperationType;
import com.gitee.dorive.api.entity.element.PropChain;
import com.gitee.dorive.core.api.common.Binder;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.context.Selector;
import com.gitee.dorive.core.api.executor.EntityHandler;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.entity.operation.Operation;
import com.gitee.dorive.core.entity.operation.Query;
import com.gitee.dorive.core.impl.factory.OperationFactory;
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
        Assert.isTrue(!query.isEmpty(), "The query cannot be empty!");

        Selector selector = context.getSelector();
        boolean isIncludeRoot = (query.getType() & OperationType.INCLUDE_ROOT) == OperationType.INCLUDE_ROOT;
        CommonRepository repository = this.repository.getRootRepository();
        
        if (selector.matches(context, repository) || isIncludeRoot) {
            Result<Object> result = repository.executeQuery(context, query);
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
        int expectedType = operation.getType();

        boolean isIncludeRoot = (expectedType & OperationType.INCLUDE_ROOT) == OperationType.INCLUDE_ROOT;
        boolean isIgnoreRoot = (expectedType & OperationType.IGNORE_ROOT) == OperationType.IGNORE_ROOT;
        int realExpectedType = expectedType & OperationType.INSERT_OR_UPDATE_OR_DELETE;

        boolean isInsertContext = (realExpectedType & OperationType.INSERT) == OperationType.INSERT;
        int expectedIncludeRoot = realExpectedType | OperationType.INCLUDE_ROOT;
        int expectedIgnoreRoot = realExpectedType | OperationType.IGNORE_ROOT;

        Object rootEntity = operation.getEntity();
        Assert.notNull(rootEntity, "The rootEntity cannot be null!");

        DelegateResolver delegateResolver = repository.getDelegateResolver();
        AbstractContextRepository<?, ?> delegateRepository = delegateResolver.delegateRepository(rootEntity);
        delegateRepository = delegateRepository == null ? repository : delegateRepository;

        Selector selector = context.getSelector();
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
                    int operationType = OperationType.NONE;
                    boolean operable = false;
                    if (isMatch) {
                        operationType = mergeOperationType(realExpectedType, repository, entity);
                        operable = (operationType & OperationType.INSERT_OR_UPDATE_OR_DELETE) > 0;
                        if ((operationType & OperationType.INSERT) == OperationType.INSERT) {
                            getBoundValue(repository, context, rootEntity, entity);
                        }
                    }
                    if (isAggregated) {
                        Operation newOperation = newOperation(realExpectedType, repository, context, entity);
                        newOperation.setType(operable ? expectedIncludeRoot : expectedIgnoreRoot);
                        totalCount += repository.execute(context, newOperation);

                    } else if (operable) {
                        if (isRoot && realExpectedType == operationType) {
                            totalCount += repository.execute(context, operation);
                        } else {
                            totalCount += doExecute(operationType, repository, context, entity);
                        }
                    }
                }
                if (isInsertContext && collection.size() == 1) {
                    setBoundId(repository, context, rootEntity, targetEntity);
                }
            }
        }
        return totalCount;
    }

    private int mergeOperationType(int realExpectedType, CommonRepository repository, Object entity) {
        if (realExpectedType == OperationType.FORCE_INSERT) {
            return OperationType.INSERT;
        } else {
            Object primaryKey = repository.getPrimaryKey(entity);
            int operationType = primaryKey == null ? OperationType.INSERT : OperationType.UPDATE_OR_DELETE;
            return realExpectedType & operationType;
        }
    }

    private Operation newOperation(int realExpectedType, CommonRepository repository, Context context, Object entity) {
        OperationFactory operationFactory = repository.getOperationFactory();
        if (realExpectedType == OperationType.INSERT) {
            return operationFactory.buildInsert(entity);

        } else if (realExpectedType == OperationType.UPDATE) {
            return operationFactory.buildUpdate(entity);

        } else if (realExpectedType == OperationType.INSERT_OR_UPDATE) {
            return new Operation(OperationType.INSERT_OR_UPDATE, entity);

        } else if (realExpectedType == OperationType.DELETE) {
            return operationFactory.buildDeleteByEntity(entity);
        }
        throw new RuntimeException("Unsupported type!");
    }

    private int doExecute(int operationType, CommonRepository repository, Context context, Object entity) {
        if (operationType == OperationType.INSERT) {
            return repository.insert(context, entity);

        } else if (operationType == OperationType.UPDATE) {
            return repository.update(context, entity);

        } else if (operationType == OperationType.DELETE) {
            return repository.delete(context, entity);
        }
        return 0;
    }

    private void getBoundValue(CommonRepository repository, Context context, Object rootEntity, Object entity) {
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

    private void setBoundId(CommonRepository repository, Context context, Object rootEntity, Object entity) {
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
