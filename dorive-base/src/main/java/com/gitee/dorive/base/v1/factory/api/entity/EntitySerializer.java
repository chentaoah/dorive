/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.factory.api.entity;

import com.gitee.dorive.base.v1.executor.api.Context;

public interface EntitySerializer {

    Object serialize(Context context, Object object);

}
