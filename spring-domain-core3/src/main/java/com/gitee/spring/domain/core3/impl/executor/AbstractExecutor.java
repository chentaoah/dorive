package com.gitee.spring.domain.core3.impl.executor;

import cn.hutool.core.bean.BeanUtil;
import com.gitee.spring.domain.core3.api.Executor;
import com.gitee.spring.domain.core3.entity.BoundedContext;
import com.gitee.spring.domain.core3.entity.executor.*;

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
    public Insert buildInsert(BoundedContext boundedContext, Object entity) {
        Object primaryKey = BeanUtil.getFieldValue(entity, "id");
        return primaryKey == null ? new Insert(entity) : null;
    }

    @Override
    public Update buildUpdate(BoundedContext boundedContext, Object entity) {
        Object primaryKey = BeanUtil.getFieldValue(entity, "id");
        return primaryKey != null ? new Update(entity, primaryKey) : null;
    }

    @Override
    public Update buildUpdate(BoundedContext boundedContext, Object entity, Example example) {
        return new Update(entity, example);
    }

    @Override
    public Operation buildInsertOrUpdate(BoundedContext boundedContext, Object entity) {
        Object primaryKey = BeanUtil.getFieldValue(entity, "id");
        return primaryKey == null ? new Insert(entity) : new Update(entity, primaryKey);
    }

    @Override
    public Delete buildDelete(BoundedContext boundedContext, Object entity) {
        Object primaryKey = BeanUtil.getFieldValue(entity, "id");
        return primaryKey != null ? new Delete(primaryKey) : null;
    }

    @Override
    public Delete buildDeleteByPK(BoundedContext boundedContext, Object primaryKey) {
        return new Delete(primaryKey);
    }

    @Override
    public Delete buildDelete(BoundedContext boundedContext, Example example) {
        return new Delete(example);
    }

}
