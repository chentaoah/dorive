/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.executor.v1.api;

import com.gitee.dorive.base.v1.repository.api.RepositoryItem;

public interface IndexProvider {

    int indexOf(RepositoryItem repositoryItem);

}
