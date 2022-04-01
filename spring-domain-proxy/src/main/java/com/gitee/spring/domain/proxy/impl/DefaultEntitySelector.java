package com.gitee.spring.domain.proxy.impl;

import com.gitee.spring.domain.proxy.api.EntitySelector;
import com.gitee.spring.domain.proxy.entity.BoundedContext;
import com.gitee.spring.domain.proxy.entity.EntityDefinition;

import java.util.List;

public class DefaultEntitySelector implements EntitySelector {

    @Override
    public Object select(AbstractGenericRepository<?, ?> repository, BoundedContext boundedContext, Object rootEntity, EntityDefinition entityDefinition) {
        Object queryParams = repository.getQueryParamsFromContext(boundedContext, rootEntity, entityDefinition);
        List<?> persistentObjects = repository.doSelectByExample(entityDefinition.getMapper(), boundedContext, queryParams, null);
        if (persistentObjects != null && !persistentObjects.isEmpty()) {
            return repository.assembleEntity(boundedContext, rootEntity, entityDefinition, persistentObjects);
        }
        return null;
    }

}