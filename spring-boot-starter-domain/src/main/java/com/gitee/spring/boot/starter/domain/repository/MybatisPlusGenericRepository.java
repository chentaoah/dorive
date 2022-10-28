package com.gitee.spring.boot.starter.domain.repository;

import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.repository.AbstractRepository;
import com.gitee.spring.domain.web.repository.AbstractWebRepository;

import java.io.Serializable;

@Deprecated
public class MybatisPlusGenericRepository<E, PK extends Serializable> extends AbstractWebRepository<E, PK> {

    @Override
    protected EntityMapper newEntityMapper(EntityDefinition entityDefinition) {
        return new MybatisPlusEntityMapper(entityDefinition);
    }

    @Override
    protected AbstractRepository<Object, Object> newRepository(EntityDefinition entityDefinition) {
        return new MybatisPlusRepository(entityDefinition);
    }

}
