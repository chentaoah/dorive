/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.module.v1.api;

public interface ModuleChecker {

    void checkInjection(Class<?> type, Class<?> injectedType, Object injectedBean);

}
