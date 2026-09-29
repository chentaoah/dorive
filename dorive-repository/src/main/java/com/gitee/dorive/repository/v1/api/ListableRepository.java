/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.repository.v1.api;

import com.gitee.dorive.base.v1.executor.api.Options;
import com.gitee.dorive.base.v1.repository.api.Repository;

import java.util.List;

public interface ListableRepository<E, PK> extends Repository<E, PK> {

    int insertList(Options options, List<E> entities);

    int updateList(Options options, List<E> entities);

    int insertOrUpdateList(Options options, List<E> entities);

    int deleteList(Options options, List<E> entities);

}
