package com.gitee.spring.domain.proxy.api;

import com.gitee.spring.domain.proxy.entity.BoundedContext;

import java.util.List;

public interface ChainRepository<E, PK> {

    List<E> findByChainQuery(BoundedContext boundedContext, PK chainQuery);

    List<E> findByChainQuery(PK chainQuery);

    <T> T findPageByChainQuery(BoundedContext boundedContext, PK chainQuery, Object page);

    <T> T findPageByChainQuery(PK chainQuery, Object page);

}
