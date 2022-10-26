package com.gitee.spring.domain.core3.impl.executor;

import com.gitee.spring.domain.core3.api.Executor;
import com.gitee.spring.domain.core3.entity.BoundedContext;
import com.gitee.spring.domain.core3.entity.executor.Example;
import com.gitee.spring.domain.core3.entity.executor.Operation;
import com.gitee.spring.domain.core3.entity.executor.Query;

public abstract class AbstractExecutor implements Executor {

    @Override
    public Query buildQueryByPK(BoundedContext boundedContext, Object primaryKey) {
        return new Query(primaryKey);
    }

    @Override
    public Query buildQuery(BoundedContext boundedContext, Example example) {
        return new Query(example);
    }

    @Override
    public Operation buildInsert(BoundedContext boundedContext, Object entity) {
        return null;
    }

    @Override
    public Operation buildUpdate(BoundedContext boundedContext, Object entity) {
        return null;
    }

    @Override
    public Operation buildUpdate(BoundedContext boundedContext, Object entity, Example example) {
        return null;
    }

    @Override
    public Operation buildInsertOrUpdate(BoundedContext boundedContext, Object entity) {
        return null;
    }

    @Override
    public Operation buildDelete(BoundedContext boundedContext, Object entity) {
        return null;
    }

    @Override
    public Operation buildDeleteByPK(BoundedContext boundedContext, Object primaryKey) {
        return null;
    }

    @Override
    public Operation buildDelete(BoundedContext boundedContext, Example example) {
        return null;
    }

}
