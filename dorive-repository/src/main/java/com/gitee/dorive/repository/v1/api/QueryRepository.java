/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.repository.v1.api;

import com.gitee.dorive.base.v1.executor.api.Options;
import com.gitee.dorive.base.v1.executor.entity.qry.Page;

import java.util.List;

public interface QueryRepository<E, PK> {

    List<E> selectByQuery(Options options, Object query);

    Page<E> selectPageByQuery(Options options, Object query);

    long selectCountByQuery(Options options, Object query);

}
