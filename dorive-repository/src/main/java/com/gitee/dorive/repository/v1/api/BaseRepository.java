/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.repository.v1.api;

import com.gitee.dorive.base.v1.mybatis.api.CountQuerier;

public interface BaseRepository<E, PK> extends GenericRepository<E, PK>, QueryRepository<E, PK>, CountQuerier {
}
