/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.event.api;

import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.base.v1.executor.entity.eop.EntityOp;

public interface EventPublisher {

    void publishEvent(Class<?> entityClass, Context context, EntityOp entityOp);

}
