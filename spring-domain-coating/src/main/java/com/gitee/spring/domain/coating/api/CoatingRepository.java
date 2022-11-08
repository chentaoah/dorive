package com.gitee.spring.domain.coating.api;

import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.executor.Page;

import java.util.List;

public interface CoatingRepository<E, PK> {

    List<E> selectByCoating(BoundedContext boundedContext, Object coatingObject);

    Page<E> selectPageByCoating(BoundedContext boundedContext, Object coatingObject);

}
