package com.gitee.spring.domain.core3.repository;

import cn.hutool.core.lang.Assert;
import com.gitee.spring.domain.core3.api.Executor;
import com.gitee.spring.domain.core3.api.Repository;
import com.gitee.spring.domain.core3.entity.*;
import com.gitee.spring.domain.core3.entity.definition.EntityDefinition;
import com.gitee.spring.domain.core3.entity.definition.ElementDefinition;
import com.gitee.spring.domain.core3.entity.executor.*;
import com.gitee.spring.domain.core3.entity.operation.Delete;
import com.gitee.spring.domain.core3.entity.operation.Insert;
import com.gitee.spring.domain.core3.entity.operation.Operation;
import com.gitee.spring.domain.core3.entity.operation.Query;
import com.gitee.spring.domain.core3.entity.operation.Update;
import lombok.Data;

import java.util.List;

@Data
public abstract class AbstractRepository<E, PK> implements Repository<E, PK> {

    protected ElementDefinition elementDefinition;
    protected EntityDefinition entityDefinition;
    protected Executor executor;

    @Override
    @SuppressWarnings("unchecked")
    public E selectByPrimaryKey(BoundedContext boundedContext, PK primaryKey) {
        Query query = executor.buildQueryByPK(boundedContext, primaryKey);
        Result result = executor.executeQuery(boundedContext, query);
        return (E) result.getRecord();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<E> selectByExample(BoundedContext boundedContext, Example example) {
        Query query = executor.buildQuery(boundedContext, example);
        Result result = executor.executeQuery(boundedContext, query);
        return (List<E>) result.getRecords();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Page<E> selectPageByExample(BoundedContext boundedContext, Example example) {
        Assert.notNull(example.getPage(), "The page cannot be null!");
        Query query = executor.buildQuery(boundedContext, example);
        Result result = executor.executeQuery(boundedContext, query);
        return (Page<E>) result.getPage();
    }

    @Override
    public int insert(BoundedContext boundedContext, E entity) {
        Insert insert = executor.buildInsert(boundedContext, entity);
        return executor.execute(boundedContext, insert);
    }

    @Override
    public int update(BoundedContext boundedContext, E entity) {
        Update update = executor.buildUpdate(boundedContext, entity);
        return executor.execute(boundedContext, update);
    }

    @Override
    public int updateByExample(BoundedContext boundedContext, Object entity, Example example) {
        Update update = executor.buildUpdate(boundedContext, entity, example);
        return executor.execute(boundedContext, update);
    }

    @Override
    public int insertOrUpdate(BoundedContext boundedContext, E entity) {
        Operation operation = executor.buildInsertOrUpdate(boundedContext, entity);
        return executor.execute(boundedContext, operation);
    }

    @Override
    public int delete(BoundedContext boundedContext, E entity) {
        Delete delete = executor.buildDelete(boundedContext, entity);
        return executor.execute(boundedContext, delete);
    }

    @Override
    public int deleteByPrimaryKey(BoundedContext boundedContext, PK primaryKey) {
        Delete delete = executor.buildDeleteByPK(boundedContext, primaryKey);
        return executor.execute(boundedContext, delete);
    }

    @Override
    public int deleteByExample(BoundedContext boundedContext, Example example) {
        Delete delete = executor.buildDelete(boundedContext, example);
        return executor.execute(boundedContext, delete);
    }

}
