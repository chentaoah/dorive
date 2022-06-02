package com.gitee.spring.boot.starter.domain.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.repository.AbstractRepository;
import com.gitee.spring.domain.web.repository.AbstractWebRepository;

import java.io.Serializable;

public class MybatisPlusGenericRepository<E, PK extends Serializable> extends AbstractWebRepository<E, PK> {

    @Override
    protected EntityMapper newEntityMapper(EntityDefinition entityDefinition) {
        return new MybatisPlusEntityMapper();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected AbstractRepository<Object, Object> newRepository(EntityDefinition entityDefinition) {
        BaseMapper<Object> baseMapper = (BaseMapper<Object>) entityDefinition.getMapper();
        return new MybatisPlusRepository(baseMapper);
    }

}
