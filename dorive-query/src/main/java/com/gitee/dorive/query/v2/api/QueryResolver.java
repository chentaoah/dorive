/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.query.v2.api;

import com.gitee.dorive.base.v1.executor.api.Context;

public interface QueryResolver {

    Object resolve(Context context, Object query);

}
