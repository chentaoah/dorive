package com.gitee.spring.domain.core3.impl.executor;

import com.gitee.spring.domain.core3.api.EntityHandler;
import com.gitee.spring.domain.core3.entity.BoundedContext;
import com.gitee.spring.domain.core3.entity.executor.*;
import com.gitee.spring.domain.core3.repository.AbstractContextRepository;
import com.gitee.spring.domain.core3.repository.ConfiguredRepository;

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
    public int execute(BoundedContext boundedContext, Operation operation) {
        if (operation instanceof Insert) {

        } else if (operation instanceof Update) {

        } else if (operation instanceof Delete) {

        }
        return 0;
    }

}
