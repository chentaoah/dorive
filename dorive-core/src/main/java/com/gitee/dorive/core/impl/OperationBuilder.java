package com.gitee.dorive.core.impl;

import com.gitee.dorive.core.entity.BoundedContext;
import com.gitee.dorive.core.entity.EntityElement;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.operation.*;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OperationBuilder {

    protected EntityElement entityElement;

    public Query buildQueryByPK(BoundedContext boundedContext, Object primaryKey) {
        Query query = new Query(Operation.SELECT, null);
        query.setPrimaryKey(primaryKey);
        return query;
    }

    public Query buildQuery(BoundedContext boundedContext, Example example) {
        Query query = new Query(Operation.SELECT, null);
        query.setExample(example);
        return query;
    }

    public Insert buildInsert(BoundedContext boundedContext, Object entity) {
        return new Insert(Operation.INSERT, entity);
    }

    public Update buildUpdate(BoundedContext boundedContext, Object entity) {
        Update update = new Update(Operation.UPDATE, entity);
        Object primaryKey = entityElement.getPrimaryKeyProxy().getValue(entity);
        update.setPrimaryKey(primaryKey);
        return update;
    }

    public Update buildUpdate(BoundedContext boundedContext, Object entity, Example example) {
        Update update = new Update(Operation.UPDATE, entity);
        update.setExample(example);
        return update;
    }

    public Operation buildInsertOrUpdate(BoundedContext boundedContext, Object entity) {
        Object primaryKey = entityElement.getPrimaryKeyProxy().getValue(entity);
        if (primaryKey == null) {
            return new Insert(Operation.INSERT, entity);
        } else {
            Update update = new Update(Operation.UPDATE, entity);
            update.setPrimaryKey(primaryKey);
            return update;
        }
    }

    public Delete buildDelete(BoundedContext boundedContext, Object entity) {
        Delete delete = new Delete(Operation.DELETE, entity);
        Object primaryKey = entityElement.getPrimaryKeyProxy().getValue(entity);
        delete.setPrimaryKey(primaryKey);
        return delete;
    }

    public Delete buildDeleteByPK(BoundedContext boundedContext, Object primaryKey) {
        Delete delete = new Delete(Operation.DELETE, null);
        delete.setPrimaryKey(primaryKey);
        return delete;
    }

    public Delete buildDelete(BoundedContext boundedContext, Example example) {
        Delete delete = new Delete(Operation.DELETE, null);
        delete.setExample(example);
        return delete;
    }

}
