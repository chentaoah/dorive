package com.gitee.spring.domain.core3.impl.handler;

import com.gitee.spring.domain.core3.api.EntityHandler;
import com.gitee.spring.domain.core3.entity.BoundedContext;
import com.gitee.spring.domain.core3.repository.AbstractContextRepository;

import java.util.List;

public class BatchEntityHandler implements EntityHandler {

    private final AbstractContextRepository<?, ?> repository;

    public BatchEntityHandler(AbstractContextRepository<?, ?> repository) {
        this.repository = repository;
    }

    @Override
    public void handleEntities(BoundedContext boundedContext, List<Object> rootEntities) {

    }

}
