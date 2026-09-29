/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.executor.api;

import com.gitee.dorive.base.v1.executor.entity.cop.Condition;

public interface ConditionHandler {

    long handle(Context context, Condition condition);

}
