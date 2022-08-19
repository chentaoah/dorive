package com.gitee.spring.domain.core.api;

import com.gitee.spring.domain.core.entity.BoundedContext;

import java.util.List;

public interface IRepository<E, PK> {

    E selectByPrimaryKey(BoundedContext boundedContext, PK primaryKey);

    List<E> selectByExample(BoundedContext boundedContext, Object example);

    <T> T selectPageByExample(BoundedContext boundedContext, Object example, Object page);

    int insert(BoundedContext boundedContext, E entity);

    int updateSelective(BoundedContext boundedContext, E entity);

    int update(BoundedContext boundedContext, E entity);

    int updateByExample(BoundedContext boundedContext, Object entity, Object example);

    int insertOrUpdate(BoundedContext boundedContext, E entity);

    int delete(BoundedContext boundedContext, E entity);

    int deleteByPrimaryKey(BoundedContext boundedContext, PK primaryKey);

    int deleteByExample(BoundedContext boundedContext, Object example);

}
