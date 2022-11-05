package com.gitee.spring.domain.core.api;

import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.executor.Example;
import com.gitee.spring.domain.core.entity.executor.Page;

import java.util.List;

public interface Repository<E, PK> {

    E selectByPrimaryKey(BoundedContext boundedContext, PK primaryKey);

    List<E> selectByExample(BoundedContext boundedContext, Example example);

    Page<E> selectPageByExample(BoundedContext boundedContext, Example example);

    int insert(BoundedContext boundedContext, E entity);

    int update(BoundedContext boundedContext, E entity);

    int updateByExample(BoundedContext boundedContext, Object entity, Example example);

    int insertOrUpdate(BoundedContext boundedContext, E entity);

    int delete(BoundedContext boundedContext, E entity);

    int deleteByPrimaryKey(BoundedContext boundedContext, PK primaryKey);

    int deleteByExample(BoundedContext boundedContext, Example example);

}
