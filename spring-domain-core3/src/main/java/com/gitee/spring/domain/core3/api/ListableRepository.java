package com.gitee.spring.domain.core3.api;

import com.gitee.spring.domain.core3.entity.BoundedContext;

import java.util.List;

public interface ListableRepository<E, PK> extends Repository<E, PK> {

    int insertList(BoundedContext boundedContext, List<E> entities);

    int updateList(BoundedContext boundedContext, List<E> entities);

    int insertOrUpdateList(BoundedContext boundedContext, List<E> entities);

    int deleteList(BoundedContext boundedContext, List<E> entities);

}
