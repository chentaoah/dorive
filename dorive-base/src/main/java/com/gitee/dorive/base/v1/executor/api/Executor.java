/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.executor.api;

import com.gitee.dorive.base.v1.executor.entity.op.Result;
import com.gitee.dorive.base.v1.executor.entity.op.Operation;
import com.gitee.dorive.base.v1.executor.entity.cop.Query;

public interface Executor {

    Result<Object> executeQuery(Context context, Query query);

    long executeCount(Context context, Query query);

    int execute(Context context, Operation operation);

}
