package com.gitee.spring.domain.core3.impl.executor;

import com.gitee.spring.domain.core3.api.EntityHandler;
import com.gitee.spring.domain.core3.entity.BoundedContext;
import com.gitee.spring.domain.core3.entity.executor.*;
import com.gitee.spring.domain.core3.impl.handler.BatchEntityHandler;
import com.gitee.spring.domain.core3.repository.AbstractContextRepository;
import com.gitee.spring.domain.core3.repository.ConfiguredRepository;

import java.util.Collections;
import java.util.List;

public class ChainExecutor extends AbstractExecutor {

    private final AbstractContextRepository<?, ?> repository;
    private final EntityHandler entityHandler;

    public ChainExecutor(AbstractContextRepository<?, ?> repository) {
        this.repository = repository;
        this.entityHandler = new BatchEntityHandler(repository);
    }

    @Override
    public Result executeQuery(BoundedContext boundedContext, Query query) {
        ConfiguredRepository rootRepository = repository.getRootRepository();
        if (query.getPrimaryKey() != null) {
            Object rootEntity = rootRepository.selectByPrimaryKey(boundedContext, query.getPrimaryKey());
            entityHandler.handleEntities(boundedContext, Collections.singletonList(rootEntity));
            return new Result(rootEntity);

        } else if (!query.startPage()) {
            List<Object> rootEntities = rootRepository.selectByExample(boundedContext, query.getExample());
            entityHandler.handleEntities(boundedContext, rootEntities);
            return new Result(rootEntities);

        } else {
            Page<Object> page = rootRepository.selectPageByExample(boundedContext, query.getExample());
            entityHandler.handleEntities(boundedContext, page.getRecords());
            return new Result(page);
        }
    }

    @Override
    public int execute(BoundedContext boundedContext, Operation operation) {
        return 0;
    }

}
