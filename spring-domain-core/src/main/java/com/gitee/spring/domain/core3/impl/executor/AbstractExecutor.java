package com.gitee.spring.domain.core3.impl.executor;

import cn.hutool.core.bean.BeanUtil;
import com.gitee.spring.domain.core3.api.Executor;
import com.gitee.spring.domain.core3.entity.BoundedContext;
import com.gitee.spring.domain.core3.entity.executor.*;
import com.gitee.spring.domain.core3.entity.operation.Delete;
import com.gitee.spring.domain.core3.entity.operation.Insert;
import com.gitee.spring.domain.core3.entity.operation.Operation;
import com.gitee.spring.domain.core3.entity.operation.Query;
import com.gitee.spring.domain.core3.entity.operation.Update;

public abstract class AbstractExecutor implements Executor {

    @Override
    public Query buildQueryByPK(BoundedContext boundedContext, Object primaryKey) {
        Query query = new Query(Operation.SELECT, null);
        query.setPrimaryKey(primaryKey);
        return query;
    }

    @Override
    public Query buildQuery(BoundedContext boundedContext, Example example) {
        Query query = new Query(Operation.SELECT, null);
        query.setExample(example);
        return query;
    }

    @Override
    public Insert buildInsert(BoundedContext boundedContext, Object entity) {
        return new Insert(Operation.INSERT, entity);
    }

    @Override
    public Update buildUpdate(BoundedContext boundedContext, Object entity) {
        Update update = new Update(Operation.UPDATE, entity);
        update.setPrimaryKey(BeanUtil.getFieldValue(entity, "id"));
        return update;
    }

    @Override
    public Update buildUpdate(BoundedContext boundedContext, Object entity, Example example) {
        Update update = new Update(Operation.UPDATE, entity);
        update.setExample(example);
        return update;
    }

    @Override
    public Operation buildInsertOrUpdate(BoundedContext boundedContext, Object entity) {
        Object primaryKey = BeanUtil.getFieldValue(entity, "id");
        if (primaryKey == null) {
            return new Insert(Operation.INSERT, entity);
        } else {
            Update update = new Update(Operation.UPDATE, entity);
            update.setPrimaryKey(primaryKey);
            return update;
        }
    }

    @Override
    public Delete buildDelete(BoundedContext boundedContext, Object entity) {
        Delete delete = new Delete(Operation.DELETE, entity);
        delete.setPrimaryKey(BeanUtil.getFieldValue(entity, "id"));
        return delete;
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

}
