package com.gitee.spring.domain.proxy.impl;

import com.gitee.spring.domain.proxy.api.EntitySelector;
import com.gitee.spring.domain.proxy.entity.BoundedContext;
import com.gitee.spring.domain.proxy.entity.EntityDefinition;

import java.util.List;
import java.util.Map;

public class DefaultEntitySelector implements EntitySelector {

    @Override
    public Object select(AbstractGenericRepository<?, ?> repository, BoundedContext boundedContext, Object rootEntity, EntityDefinition entityDefinition) {
        Map<String, Object> queryParams = repository.getQueryParamsFromContext(boundedContext, rootEntity, entityDefinition);
        List<?> persistentObjects = repository.doSelectByExample(entityDefinition.getMapper(), boundedContext, queryParams);
        if (persistentObjects != null && !persistentObjects.isEmpty()) {
            return repository.assembleEntity(boundedContext, rootEntity, entityDefinition, persistentObjects);
        }
        return null;
    }

}
