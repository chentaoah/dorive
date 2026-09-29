/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.joiner.v1.api;

import com.gitee.dorive.base.v1.executor.api.Context;

public interface KeyGenerator {

    String generate(Context context, Object entity);

}
