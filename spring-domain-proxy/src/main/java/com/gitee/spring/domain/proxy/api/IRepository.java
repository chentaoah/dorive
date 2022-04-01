package com.gitee.spring.domain.proxy.api;

import com.gitee.spring.domain.proxy.entity.BoundedContext;

import java.util.List;

public interface IRepository<E, PK> {

    E findByPrimaryKey(BoundedContext boundedContext, PK primaryKey);

    E findByPrimaryKey(PK primaryKey);

    List<E> findByExample(BoundedContext boundedContext, Object example, Object page);

    List<E> findByExample(BoundedContext boundedContext, Object example);

    List<E> findByExample(Object example);

    void insert(BoundedContext boundedContext, E entity);

    void insert(E entity);

    void update(BoundedContext boundedContext, E entity);

    void update(E entity);

    void delete(BoundedContext boundedContext, E entity);

    void delete(E entity);

    void deleteByPrimaryKey(PK primaryKey);

}
