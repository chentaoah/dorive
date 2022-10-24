package com.gitee.spring.domain.core3.api;

import com.gitee.spring.domain.core3.entity.BoundedContext;
import com.gitee.spring.domain.core3.entity.Example;
import com.gitee.spring.domain.core3.entity.Result;
import com.gitee.spring.domain.core3.entity.Operation;
import com.gitee.spring.domain.core3.entity.Query;

public interface Executor {

    Query buildQueryByPK(BoundedContext boundedContext, Object primaryKey);

    Query buildQuery(BoundedContext boundedContext, Example example);

    Result executeQuery(BoundedContext boundedContext, Query query);

    Operation buildInsert(BoundedContext boundedContext, Object entity);

    Operation buildUpdate(BoundedContext boundedContext, Object entity);

    Operation buildUpdate(BoundedContext boundedContext, Object entity, Example example);

    Operation buildInsertOrUpdate(BoundedContext boundedContext, Object entity);

    Operation buildDelete(BoundedContext boundedContext, Object entity);

    Operation buildDeleteByPK(BoundedContext boundedContext, Object primaryKey);

    Operation buildDelete(BoundedContext boundedContext, Example example);

    int execute(BoundedContext boundedContext, Operation operation);

}
