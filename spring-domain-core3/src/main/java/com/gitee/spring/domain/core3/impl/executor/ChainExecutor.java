package com.gitee.spring.domain.core3.impl.executor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import com.gitee.spring.domain.core3.api.Binder;
import com.gitee.spring.domain.core3.api.EntityHandler;
import com.gitee.spring.domain.core3.entity.BoundedContext;
import com.gitee.spring.domain.core3.entity.PropertyChain;
import com.gitee.spring.domain.core3.entity.executor.*;
import com.gitee.spring.domain.core3.entity.operation.Delete;
import com.gitee.spring.domain.core3.entity.operation.Insert;
import com.gitee.spring.domain.core3.entity.operation.Operation;
import com.gitee.spring.domain.core3.entity.operation.Query;
import com.gitee.spring.domain.core3.entity.operation.Update;
import com.gitee.spring.domain.core3.repository.AbstractContextRepository;
import com.gitee.spring.domain.core3.repository.ConfiguredRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ChainExecutor extends AbstractExecutor {

    private final AbstractContextRepository<?, ?> repository;
    private final EntityHandler entityHandler;

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
    public Insert buildInsert(BoundedContext boundedContext, Object entity) {
        return new Insert(Operation.INSERT, entity);
    }

    @Override
    public Update buildUpdate(BoundedContext boundedContext, Object entity) {
        return new Update(Operation.UPDATE, entity);
    }

    @Override
    public Update buildUpdate(BoundedContext boundedContext, Object entity, Example example) {
        Update update = new Update(Operation.UPDATE, entity);
        update.setExample(example);
        return update;
    }

    @Override
    public Operation buildInsertOrUpdate(BoundedContext boundedContext, Object entity) {
        return new Operation(Operation.INSERT_OR_UPDATE, entity);
    }

    @Override
    public Delete buildDelete(BoundedContext boundedContext, Object entity) {
        return new Delete(Operation.DELETE, entity);
    }

    @Override
    public Delete buildDeleteByPK(BoundedContext boundedContext, Object primaryKey) {
        Delete delete = new Delete(Operation.DELETE, null);
        delete.setPrimaryKey(primaryKey);
        return delete;
    }

    @Override
    public Delete buildDelete(BoundedContext boundedContext, Example example) {
        Delete delete = new Delete(Operation.DELETE, null);
        delete.setExample(example);
        return delete;
    }

    @Override
    public int execute(BoundedContext boundedContext, Operation operation) {
        int expectedOperationType = operation.getType();
        Object rootEntity = operation.getEntity();
        Assert.notNull(rootEntity, "The rootEntity cannot be null!");
        int totalCount = 0;
        for (ConfiguredRepository orderedRepository : repository.getOrderedRepositories()) {
            PropertyChain anchorPoint = orderedRepository.getAnchorPoint();
            Object targetEntity = anchorPoint == null ? rootEntity : anchorPoint.getValue(rootEntity);
            if (targetEntity != null && orderedRepository.matchContext(boundedContext)) {
                if (targetEntity instanceof Collection) {
                    for (Object eachEntity : (Collection<?>) targetEntity) {
                        int operationType = compareOperationType(expectedOperationType, eachEntity);
                        totalCount += doExecute(boundedContext, rootEntity, orderedRepository, eachEntity, operationType);
                    }
                } else {
                    int operationType = compareOperationType(expectedOperationType, targetEntity);
                    totalCount += doExecute(boundedContext, rootEntity, orderedRepository, targetEntity, operationType);
                    if (expectedOperationType == Operation.INSERT || expectedOperationType == Operation.INSERT_OR_UPDATE) {
                        setBoundIdForBoundEntity(boundedContext, rootEntity, orderedRepository, targetEntity);
                    }
                }
            }
        }
        return totalCount;
    }

    private int compareOperationType(int expectedOperationType, Object entity) {
        if (expectedOperationType == Operation.INSERT_OR_UPDATE) {
            return Operation.INSERT_OR_UPDATE;
        } else {
            Object primaryKey = BeanUtil.getFieldValue(entity, "id");
            int operationType = primaryKey == null ? Operation.INSERT : Operation.UPDATE_OR_DELETE;
            return expectedOperationType & operationType;
        }
    }

    private int doExecute(BoundedContext boundedContext, Object rootEntity, ConfiguredRepository repository, Object entity, int operationType) {
        if (operationType == Operation.INSERT) {
            getBoundValueFromContext(boundedContext, rootEntity, repository, entity);
            return repository.insert(boundedContext, entity);

        } else if (operationType == Operation.UPDATE) {
            return repository.update(boundedContext, entity);

        } else if (operationType == Operation.INSERT_OR_UPDATE) {
            getBoundValueFromContext(boundedContext, rootEntity, repository, entity);
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
            Object primaryKey = binder.getFieldValue(boundedContext, entity);
            if (primaryKey != null) {
                binder.setBoundValue(boundedContext, rootEntity, primaryKey);
            }
        }
    }

}
