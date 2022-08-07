package com.gitee.spring.domain.core.api;

import com.gitee.spring.domain.core.entity.BoundedContext;

import java.util.List;

public interface GenericRepository<E, PK> extends BaseRepository<E, PK> {

    int insertOrUpdate(BoundedContext boundedContext, E entity);

    int insertList(BoundedContext boundedContext, List<E> entities);

    int updateList(BoundedContext boundedContext, List<E> entities);

    int deleteList(BoundedContext boundedContext, List<E> entities);

    int insertOrUpdateList(BoundedContext boundedContext, List<E> entities);

}
