/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.executor.v1.impl.executor;

import cn.hutool.core.lang.Assert;
import com.gitee.dorive.base.v1.executor.api.ConditionHandler;
import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.base.v1.executor.entity.cop.Query;
import com.gitee.dorive.base.v1.executor.entity.cop.Condition;
import com.gitee.dorive.base.v1.executor.entity.eop.EntityOp;
import com.gitee.dorive.base.v1.executor.entity.op.Operation;
import com.gitee.dorive.base.v1.executor.entity.op.Result;
import com.gitee.dorive.base.v1.executor.entity.qry.Example;
import com.gitee.dorive.base.v1.executor.impl.executor.AbstractExecutor;
import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import com.gitee.dorive.base.v1.executor.api.EntityHandler;
import com.gitee.dorive.base.v1.executor.api.EntityOpHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class RepositoryExecutor extends AbstractExecutor implements EntityHandler, EntityOpHandler, ConditionHandler {

    private final RepositoryContext repositoryContext;
    private final EntityHandler entityHandler;
    private final EntityOpHandler entityOpHandler;
    private final ConditionHandler conditionHandler;

    @Override
    public Result<Object> executeQuery(Context context, Query query) {
        Assert.isTrue(!query.isEmpty(), "The query cannot be empty!");
        RepositoryItem rootRepository = repositoryContext.getRootRepository();
        if (repositoryContext.matches(context, query, rootRepository)) {
            Result<Object> result = rootRepository.executeQuery(context, query);
            List<Object> entities = result.getRecords();
            if (!entities.isEmpty()) {
                handle(context, entities);
            }
            return result;
        }
        return Result.emptyResult(query);
    }

    @Override
    public long handle(Context context, List<Object> entities) {
        return entityHandler.handle(context, entities);
    }

    @Override
    public long executeCount(Context context, Query query) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int execute(Context context, Operation operation) {
        if (operation instanceof EntityOp entityOp) {
            List<?> entities = entityOp.getEntities();
            Assert.notEmpty(entities, "The entities cannot be empty!");
            return (int) handle(context, entityOp);

        } else if (operation instanceof Condition condition) {
            Example example = condition.getExample();
            Assert.notNull(example, "The example cannot be null!");
            return (int) handle(context, condition);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public long handle(Context context, EntityOp entityOp) {
        return entityOpHandler.handle(context, entityOp);
    }

    @Override
    public long handle(Context context, Condition condition) {
        return conditionHandler.handle(context, condition);
    }

}
