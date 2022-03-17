package com.gitee.spring.domain.proxy.impl;

import com.gitee.spring.domain.proxy.api.IRepository;

public abstract class AbstractRepository<E, PK> implements IRepository<E, PK> {

    @Override
    public E findByPrimaryKey(PK primaryKey) {
        return null;
    }

    @Override
    public boolean insert(E entity) {
        return false;
    }

    @Override
    public boolean update(E entity) {
        return false;
    }

    @Override
    public boolean delete(E entity) {
        return false;
    }

}
