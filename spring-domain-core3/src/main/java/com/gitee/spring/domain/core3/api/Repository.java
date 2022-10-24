package com.gitee.spring.domain.core3.api;

import com.gitee.spring.domain.core3.entity.BoundedContext;
import com.gitee.spring.domain.core3.entity.Example;

import java.util.List;

public interface Repository<E, PK> {

    E selectByPrimaryKey(BoundedContext boundedContext, PK primaryKey);

    List<E> selectByExample(BoundedContext boundedContext, Example example);

    <T> T selectPageByExample(BoundedContext boundedContext, Example example);

    int insert(BoundedContext boundedContext, E entity);

    int update(BoundedContext boundedContext, E entity);

    int updateByExample(BoundedContext boundedContext, Object entity, Example example);

    int insertOrUpdate(BoundedContext boundedContext, E entity);

    int delete(BoundedContext boundedContext, E entity);

    int deleteByPrimaryKey(BoundedContext boundedContext, PK primaryKey);

    int deleteByExample(BoundedContext boundedContext, Example example);

}
