package com.gitee.spring.domain.proxy.impl;

import cn.hutool.core.lang.Assert;
import com.gitee.spring.domain.proxy.api.EntityRepository;
import com.gitee.spring.domain.proxy.entity.EntityDefinition;

public abstract class AbstractEntityRepository<E, PK> extends AbstractChainRepository<E, PK> implements EntityRepository {

    @Override
    public void updateByExample(Object entity, Object example) {
        Class<?> entityClass = entity.getClass();
        EntityDefinition entityDefinition = classEntityDefinitionMap.get(entityClass);
        Assert.notNull(entityDefinition, "The entity definition does not exist!");
        doUpdateByExample(entityDefinition.getMapper(), entity, example);
    }

    @Override
    public void deleteByExample(Class<?> entityClass, Object example) {
        EntityDefinition entityDefinition = classEntityDefinitionMap.get(entityClass);
        Assert.notNull(entityDefinition, "The entity definition does not exist!");
        doDeleteByExample(entityDefinition.getMapper(), example);
    }

    protected abstract void doUpdateByExample(Object mapper, Object entity, Object example);

    protected abstract void doDeleteByExample(Object mapper, Object example);

}
