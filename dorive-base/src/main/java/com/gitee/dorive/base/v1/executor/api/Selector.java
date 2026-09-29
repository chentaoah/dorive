/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.executor.api;

import com.gitee.dorive.base.v1.repository.api.RepositoryItem;

import java.util.List;

public interface Selector extends Matcher {

    List<String> select(RepositoryItem repositoryItem);

}
