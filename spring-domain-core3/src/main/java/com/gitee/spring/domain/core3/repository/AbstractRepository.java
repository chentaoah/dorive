package com.gitee.spring.domain.core3.repository;

import com.gitee.spring.domain.core3.api.Executor;
import com.gitee.spring.domain.core3.api.Repository;
import com.gitee.spring.domain.core3.entity.*;
import com.gitee.spring.domain.core3.entity.definition.EntityDefinition;
import com.gitee.spring.domain.core3.entity.definition.ElementDefinition;
import lombok.Data;

import java.util.List;

@Data
public abstract class AbstractRepository<E, PK> implements Repository<E, PK> {

    protected ElementDefinition elementDefinition;
    protected EntityDefinition entityDefinition;
    protected boolean boundEntity;
    protected Executor executor;

    @Override
    @SuppressWarnings("unchecked")
    public E selectByPrimaryKey(BoundedContext boundedContext, PK primaryKey) {
        Query query = executor.buildQueryByPK(boundedContext, primaryKey);
        Result result = executor.executeQuery(boundedContext, query);
        return (E) result.getOne();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<E> selectByExample(BoundedContext boundedContext, Example example) {
        Query query = executor.buildQuery(boundedContext, example);
        Result result = executor.executeQuery(boundedContext, query);
        return (List<E>) result.getList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T selectPageByExample(BoundedContext boundedContext, Example example) {
        Query query = executor.buildQuery(boundedContext, example);
        Result result = executor.executeQuery(boundedContext, query);
        return (T) result.getPage();
    }

    @Override
    public int insert(BoundedContext boundedContext, E entity) {
        Operation operation = executor.buildInsert(boundedContext, entity);
        return executor.execute(boundedContext, operation);
    }

    @Override
    public int update(BoundedContext boundedContext, E entity) {
        Operation operation = executor.buildUpdate(boundedContext, entity);
        return executor.execute(boundedContext, operation);
    }

    @Override
    public int updateByExample(BoundedContext boundedContext, Object entity, Example example) {
        Operation operation = executor.buildUpdate(boundedContext, entity, example);
        return executor.execute(boundedContext, operation);
    }

    @Override
    public int insertOrUpdate(BoundedContext boundedContext, E entity) {
        Operation operation = executor.buildInsertOrUpdate(boundedContext, entity);
        return executor.execute(boundedContext, operation);
    }

    @Override
    public int delete(BoundedContext boundedContext, E entity) {
        Operation operation = executor.buildDelete(boundedContext, entity);
        return executor.execute(boundedContext, operation);
    }

    @Override
    public int deleteByPrimaryKey(BoundedContext boundedContext, PK primaryKey) {
        Operation operation = executor.buildDeleteByPK(boundedContext, primaryKey);
        return executor.execute(boundedContext, operation);
    }

    @Override
    public int deleteByExample(BoundedContext boundedContext, Example example) {
        Operation operation = executor.buildDelete(boundedContext, example);
        return executor.execute(boundedContext, operation);
    }

}
