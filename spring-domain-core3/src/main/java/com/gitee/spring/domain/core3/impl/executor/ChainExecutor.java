package com.gitee.spring.domain.core3.impl.executor;

import com.gitee.spring.domain.core3.api.EntityHandler;
import com.gitee.spring.domain.core3.api.Executor;
import com.gitee.spring.domain.core3.entity.BoundedContext;
import com.gitee.spring.domain.core3.entity.executor.Example;
import com.gitee.spring.domain.core3.entity.executor.Operation;
import com.gitee.spring.domain.core3.entity.executor.Page;
import com.gitee.spring.domain.core3.entity.executor.Query;
import com.gitee.spring.domain.core3.entity.executor.Result;
import com.gitee.spring.domain.core3.impl.handler.BatchEntityHandler;
import com.gitee.spring.domain.core3.repository.AbstractContextRepository;
import com.gitee.spring.domain.core3.repository.ConfiguredRepository;

import java.util.List;

public class ChainExecutor implements Executor {

    private final AbstractContextRepository<?, ?> repository;
    private final EntityHandler entityHandler;

    public ChainExecutor(AbstractContextRepository<?, ?> repository) {
        this.repository = repository;
        this.entityHandler = new BatchEntityHandler(repository);
    }

    @Override
    public Query buildQueryByPK(BoundedContext boundedContext, Object primaryKey) {
        return new Query(primaryKey).buildExampleByPK();
    }

    @Override
    public Query buildQuery(BoundedContext boundedContext, Example example) {
        return new Query(example);
    }

    @Override
    public Result executeQuery(BoundedContext boundedContext, Query query) {
        ConfiguredRepository rootRepository = repository.getRootRepository();
        if (query.startPage()) {
            Page<Object> page = rootRepository.selectPageByExample(boundedContext, query.getExample());
            entityHandler.handleEntities(boundedContext, page.getRecords());
            return new Result(page);

        } else {
            List<Object> rootEntities = rootRepository.selectByExample(boundedContext, query.getExample());
            entityHandler.handleEntities(boundedContext, rootEntities);
            return new Result(rootEntities);
        }
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

    @Override
    public int execute(BoundedContext boundedContext, Operation operation) {
        return 0;
    }

}
