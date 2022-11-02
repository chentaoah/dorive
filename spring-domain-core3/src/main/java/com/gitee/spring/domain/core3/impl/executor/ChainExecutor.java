package com.gitee.spring.domain.core3.impl.executor;

import cn.hutool.core.lang.Assert;
import com.gitee.spring.domain.core3.api.Binder;
import com.gitee.spring.domain.core3.api.EntityHandler;
import com.gitee.spring.domain.core3.entity.BoundedContext;
import com.gitee.spring.domain.core3.entity.PropertyChain;
import com.gitee.spring.domain.core3.entity.executor.Page;
import com.gitee.spring.domain.core3.entity.executor.Result;
import com.gitee.spring.domain.core3.entity.operation.Operation;
import com.gitee.spring.domain.core3.entity.operation.Query;
import com.gitee.spring.domain.core3.impl.OperationTypeResolver;
import com.gitee.spring.domain.core3.repository.AbstractContextRepository;
import com.gitee.spring.domain.core3.repository.ConfiguredRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ChainExecutor extends AbstractExecutor {

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
                entityHandler.handleEntities(boundedContext, Collections.singletonList(rootEntity));
            }
            return new Result(rootEntity);

        } else if (query.withoutPage()) {
            List<Object> rootEntities = rootRepository.selectByExample(boundedContext, query.getExample());
            if (!rootEntities.isEmpty()) {
                entityHandler.handleEntities(boundedContext, rootEntities);
            }
            return new Result(rootEntities);

        } else {
            Page<Object> page = rootRepository.selectPageByExample(boundedContext, query.getExample());
            List<Object> rootEntities = page.getRecords();
            if (!rootEntities.isEmpty()) {
                entityHandler.handleEntities(boundedContext, rootEntities);
            }
            return new Result(page);
        }
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

        int totalCount = 0;
        for (ConfiguredRepository orderedRepository : repository.getOrderedRepositories()) {
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
