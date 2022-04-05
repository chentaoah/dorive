package com.gitee.spring.domain.proxy.repository;

import com.gitee.spring.domain.proxy.api.IRepository;
import com.gitee.spring.domain.proxy.entity.BoundedContext;

import java.util.List;

public abstract class AbstractRepository<E, PK> implements IRepository<E, PK> {

    @Override
    public E findByPrimaryKey(PK primaryKey) {
        return findByPrimaryKey(new BoundedContext(), primaryKey);
    }

    @Override
    public List<E> findByExample(Object example) {
        return findByExample(new BoundedContext(), example);
    }

    @Override
    public <T> T findPageByExample(Object example, Object page) {
        return findPageByExample(new BoundedContext(), example, page);
    }

    @Override
    public int insert(E entity) {
        return insert(new BoundedContext(), entity);
    }

    @Override
    public int update(E entity) {
        return update(new BoundedContext(), entity);
    }

    @Override
    public int delete(E entity) {
        return delete(new BoundedContext(), entity);
    }

}
