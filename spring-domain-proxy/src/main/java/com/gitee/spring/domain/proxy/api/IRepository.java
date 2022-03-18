package com.gitee.spring.domain.proxy.api;

import com.gitee.spring.domain.proxy.entity.BoundedContext;

public interface IRepository<E, PK> {

    E findByPrimaryKey(BoundedContext boundedContext, PK primaryKey);

    E findByPrimaryKey(PK primaryKey);

    void insert(E entity);

    void update(E entity);

    void delete(E entity);

}
