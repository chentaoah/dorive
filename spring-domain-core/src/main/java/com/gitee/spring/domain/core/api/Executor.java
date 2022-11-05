package com.gitee.spring.domain.core.api;

import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.executor.*;
import com.gitee.spring.domain.core.entity.operation.Delete;
import com.gitee.spring.domain.core.entity.operation.Insert;
import com.gitee.spring.domain.core.entity.operation.Operation;
import com.gitee.spring.domain.core.entity.operation.Query;
import com.gitee.spring.domain.core.entity.operation.Update;

public interface Executor {

    Query buildQueryByPK(BoundedContext boundedContext, Object primaryKey);

    Query buildQuery(BoundedContext boundedContext, Example example);

    Result executeQuery(BoundedContext boundedContext, Query query);

    Insert buildInsert(BoundedContext boundedContext, Object entity);

    Update buildUpdate(BoundedContext boundedContext, Object entity);

    Update buildUpdate(BoundedContext boundedContext, Object entity, Example example);

    Operation buildInsertOrUpdate(BoundedContext boundedContext, Object entity);

    Delete buildDelete(BoundedContext boundedContext, Object entity);

    Delete buildDeleteByPK(BoundedContext boundedContext, Object primaryKey);

    Delete buildDelete(BoundedContext boundedContext, Example example);

    int execute(BoundedContext boundedContext, Operation operation);

}
