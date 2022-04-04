package com.gitee.spring.domain.proxy.impl;

import com.gitee.spring.domain.proxy.api.EntitySelector;
import com.gitee.spring.domain.proxy.entity.BoundedContext;
import com.gitee.spring.domain.proxy.entity.EntityDefinition;

import java.util.List;

public class DefaultEntitySelector implements EntitySelector {

    @Override
    public List<?> select(AbstractGenericRepository<?, ?> repository, BoundedContext boundedContext,
                          Object rootEntity, EntityDefinition entityDefinition, Object queryParams) {
        return repository.doSelectByExample(entityDefinition.getMapper(), boundedContext, queryParams);
    }

}
