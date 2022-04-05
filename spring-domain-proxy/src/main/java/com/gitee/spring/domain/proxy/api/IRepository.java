package com.gitee.spring.domain.proxy.api;

import com.gitee.spring.domain.proxy.entity.BoundedContext;

import java.util.List;

public interface IRepository<E, PK> {

    E findByPrimaryKey(BoundedContext boundedContext, PK primaryKey);

    E findByPrimaryKey(PK primaryKey);

    List<E> findByExample(BoundedContext boundedContext, Object example);

    List<E> findByExample(Object example);

    <T> T findPageByExample(BoundedContext boundedContext, Object example, Object page);

    <T> T findPageByExample(Object example, Object page);

    int insert(BoundedContext boundedContext, E entity);

    int insert(E entity);

    int update(BoundedContext boundedContext, E entity);

    int update(E entity);

    int updateByExample(E entity, Object example);

    int delete(BoundedContext boundedContext, E entity);

    int delete(E entity);

    int deleteByPrimaryKey(PK primaryKey);

    int deleteByExample(Object example);

}
