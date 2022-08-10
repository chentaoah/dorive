package com.gitee.spring.domain.coating.api;

import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.EntityExample;

import java.util.List;

public interface CoatingRepository<E, PK> {

    EntityExample buildExample(BoundedContext boundedContext, Object coatingObject);

    List<E> selectByCoating(BoundedContext boundedContext, Object coatingObject);

    <T> T selectPageByCoating(BoundedContext boundedContext, Object coatingObject, Object page);

}
