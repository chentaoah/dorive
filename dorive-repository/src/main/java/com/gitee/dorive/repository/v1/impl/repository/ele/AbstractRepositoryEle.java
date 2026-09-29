/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.repository.v1.impl.repository.ele;

import com.gitee.dorive.base.v1.definition.entity.EntityElement;
import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.base.v1.executor.entity.cop.Query;
import com.gitee.dorive.base.v1.executor.entity.op.Operation;
import com.gitee.dorive.base.v1.executor.entity.op.Result;
import com.gitee.dorive.base.v1.executor.api.OperationFactory;
import com.gitee.dorive.base.v1.executor.api.Executor;
import com.gitee.dorive.base.v1.repository.api.RepositoryEle;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class AbstractRepositoryEle extends AbstractProperties implements RepositoryEle {

    private EntityElement entityElement;
    private OperationFactory operationFactory;
    private Executor executor;

    public Class<?> getEntityClass() {
        return entityElement.getGenericType();
    }

    @Override
    public Result<Object> executeQuery(Context context, Query query) {
        return executor.executeQuery(context, query);
    }

    @Override
    public long executeCount(Context context, Query query) {
        return executor.executeCount(context, query);
    }

    @Override
    public int execute(Context context, Operation operation) {
        return executor.execute(context, operation);
    }

}
