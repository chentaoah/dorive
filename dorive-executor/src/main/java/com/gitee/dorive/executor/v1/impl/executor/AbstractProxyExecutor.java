/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.executor.v1.impl.executor;

import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.base.v1.executor.entity.cop.Query;
import com.gitee.dorive.base.v1.executor.entity.op.Operation;
import com.gitee.dorive.base.v1.executor.entity.op.Result;
import com.gitee.dorive.base.v1.executor.api.Executor;
import com.gitee.dorive.base.v1.executor.impl.executor.AbstractExecutor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractProxyExecutor extends AbstractExecutor {

    private Executor executor;

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
