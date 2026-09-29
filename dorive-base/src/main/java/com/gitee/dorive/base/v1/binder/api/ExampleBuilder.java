/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.binder.api;

import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.base.v1.executor.entity.qry.Example;

import java.util.List;

public interface ExampleBuilder {

    Example newExample(Context context, List<Object> entities);

}
