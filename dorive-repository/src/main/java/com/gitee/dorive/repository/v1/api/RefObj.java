/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.repository.v1.api;

import com.gitee.dorive.base.v1.executor.api.Options;

public interface RefObj {

    long select(Options options);

    int insertOrUpdate(Options options);

    int delete(Options options);

}
