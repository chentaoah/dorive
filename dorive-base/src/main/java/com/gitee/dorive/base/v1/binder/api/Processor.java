/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.binder.api;

import com.gitee.dorive.base.v1.executor.api.Context;

public interface Processor {

    Object input(Context context, Object value);

    Object output(Context context, Object value);

}
