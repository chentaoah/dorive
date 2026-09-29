/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.executor.api;

import java.util.List;

public interface EntityHandler {

    long handle(Context context, List<Object> entities);

}
