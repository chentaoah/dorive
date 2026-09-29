/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.joiner.api;

import com.gitee.dorive.base.v1.executor.api.Context;

import java.util.List;

public interface EntityJoiner {

    void join(Context context, List<Object> entities1, List<Object> entities2);

}
