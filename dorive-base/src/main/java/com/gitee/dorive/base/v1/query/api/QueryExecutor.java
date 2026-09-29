/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.query.api;

import com.gitee.dorive.base.v1.executor.api.Options;
import com.gitee.dorive.base.v1.executor.entity.qry.Page;

import java.util.List;

public interface QueryExecutor {

    List<Object> selectByQuery(Options options, Object query);

    Page<Object> selectPageByQuery(Options options, Object query);

    long selectCountByQuery(Options options, Object query);

}
