/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.mybatis.api;

import java.util.Map;

public interface MethodInvoker {

    Object invoke(Map<String, Object> params);

}
