package com.gitee.spring.domain.proxy.impl;

import com.gitee.spring.domain.proxy.api.IRepository;
import com.gitee.spring.domain.proxy.entity.BoundedContext;
import com.gitee.spring.domain.proxy.entity.RepositoryContext;

import java.util.List;

public abstract class AbstractRepository<E, PK> extends AbstractEntityDefinitionResolver implements IRepository<E, PK> {

    @Override
    protected Class<?> getTargetClass() {
        return AbstractRepository.class;
    }

    @Override
    public E findByPrimaryKey(PK primaryKey) {
        return findByPrimaryKey(new BoundedContext(), primaryKey);
    }

    @Override
    public List<E> findByExample(Object example) {
        return findByExample(new BoundedContext(), example);
    }

    @Override
    public void insert(E entity) {
        insert(getBoundedContext(entity), entity);
    }

    @Override
    public void update(E entity) {
        update(getBoundedContext(entity), entity);
    }

    @Override
    public void delete(E entity) {
        delete(getBoundedContext(entity), entity);
    }

    protected BoundedContext getBoundedContext(E entity) {
        BoundedContext boundedContext = null;
        if (entity instanceof RepositoryContext) {
            RepositoryContext repositoryContext = (RepositoryContext) entity;
            boundedContext = repositoryContext.getBoundedContext();
        }
        return boundedContext == null ? new BoundedContext() : boundedContext;
    }

}
