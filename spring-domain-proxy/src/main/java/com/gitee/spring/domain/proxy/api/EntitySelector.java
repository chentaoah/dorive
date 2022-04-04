package com.gitee.spring.domain.proxy.api;

import com.gitee.spring.domain.proxy.entity.BoundedContext;
import com.gitee.spring.domain.proxy.entity.EntityDefinition;
import com.gitee.spring.domain.proxy.impl.AbstractGenericRepository;

import java.util.List;

public interface EntitySelector {

    List<?> select(AbstractGenericRepository<?, ?> repository, BoundedContext boundedContext,
                   Object rootEntity, EntityDefinition entityDefinition, Object queryParams);

}
