package com.gitee.spring.domain.core.api;

import com.gitee.spring.domain.core.entity.BoundedContext;

import java.util.List;

public interface GenericRepository<E, PK> extends SimpleRepository<E, PK> {

    int insertList(BoundedContext boundedContext, List<E> entities);

    int updateList(BoundedContext boundedContext, List<E> entities);

    int deleteList(BoundedContext boundedContext, List<E> entities);

    int forceInsert(BoundedContext boundedContext, E entity);

    int forceInsertList(BoundedContext boundedContext, List<E> entities);

}
