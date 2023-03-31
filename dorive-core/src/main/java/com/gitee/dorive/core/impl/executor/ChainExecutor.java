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
import com.gitee.dorive.core.api.common.Binder;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.executor.EntityHandler;
import com.gitee.dorive.core.api.context.Selector;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.entity.operation.Operation;
import com.gitee.dorive.core.entity.operation.Query;
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
        query.check();

        Selector selector = context.getSelector();
        CommonRepository rootRepository = repository.getRootRepository();
        boolean isIncludeRoot = (query.getType() & Operation.INCLUDE_ROOT) == Operation.INCLUDE_ROOT;

        if (selector.matches(context, rootRepository) || isIncludeRoot) {
            int totalCount = 0;

            Result<Object> result = rootRepository.executeQuery(context, query);
            totalCount += result.getTotal();

            List<Object> rootEntities = result.getRecords();
            if (!rootEntities.isEmpty()) {
                totalCount += handle(context, rootEntities);
            }

            result.setTotal(totalCount);
            return result;
        }

        return new Result<>();
    }

    @Override
    public int handle(Context context, List<Object> entities) {
        return entityHandler.handle(context, entities);
    }

    @Override
    public int execute(Context context, Operation operation) {
        int expectedOperation = operation.getType();

        boolean isInsertContext = (expectedOperation & Operation.INSERT) == Operation.INSERT;
        boolean isIgnoreRoot = (expectedOperation & Operation.IGNORE_ROOT) == Operation.IGNORE_ROOT;
        boolean isIncludeRoot = (expectedOperation & Operation.INCLUDE_ROOT) == Operation.INCLUDE_ROOT;

        int expectedIgnoreRoot = expectedOperation | Operation.IGNORE_ROOT;
        int expectedIncludeRoot = expectedOperation | Operation.INCLUDE_ROOT;

        Object rootEntity = operation.getEntity();
        Assert.notNull(rootEntity, "The rootEntity cannot be null!");

        DelegateResolver delegateResolver = repository.getDelegateResolver();
        AbstractContextRepository<?, ?> delegateRepository = delegateResolver.delegateRepository(rootEntity);
        delegateRepository = delegateRepository == null ? repository : delegateRepository;

        Selector selector = context.getSelector();
        int totalCount = 0;
        for (CommonRepository repository : delegateRepository.getOrderedRepositories()) {
            boolean root = repository.isRoot();
            if (isIgnoreRoot && root) {
                continue;
            }

            boolean isMatch = selector.matches(context, repository) || (isIncludeRoot && root);
            boolean isAggregated = repository.isAggregated();

            PropChain anchorPoint = repository.getAnchorPoint();
            Object targetEntity = root ? rootEntity : anchorPoint.getValue(rootEntity);
            if (targetEntity != null) {
                Collection<?> collection;
                if (targetEntity instanceof Collection) {
                    collection = (Collection<?>) targetEntity;
                } else {
                    collection = Collections.singletonList(targetEntity);
                }
                for (Object entity : collection) {
                    int operationType = Operation.NONE;
                    boolean operable = false;
                    if (isMatch) {
                        operationType = mergeOperationType(expectedOperation, repository, entity);
                        operable = (operationType & Operation.INSERT_OR_UPDATE_OR_DELETE) > 0;
                        if ((operationType & Operation.INSERT) == Operation.INSERT) {
                            getBoundValue(context, rootEntity, repository, entity);
                        }
                    }
                    if (isAggregated) {
                        Operation newOperation = new Operation(operable ? expectedIncludeRoot : expectedIgnoreRoot, entity);
                        totalCount += repository.execute(context, newOperation);

                    } else if (operable) {
                        if (root && expectedOperation == operationType) {
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

    private int mergeOperationType(int expectedOperation, CommonRepository repository, Object entity) {
        if (expectedOperation == Operation.FORCE_INSERT) {
            return Operation.INSERT;
        } else {
            Object primaryKey = repository.getPrimaryKey(entity);
            int operationType = primaryKey == null ? Operation.INSERT : Operation.UPDATE_OR_DELETE;
            return expectedOperation & operationType;
        }
    }

    private int doExecute(Context context, CommonRepository repository, Object entity, int operationType) {
        if (operationType == Operation.INSERT) {
            return repository.insert(context, entity);

        } else if (operationType == Operation.UPDATE) {
            return repository.update(context, entity);

        } else if (operationType == Operation.DELETE) {
            return repository.delete(context, entity);
        }
        return 0;
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
