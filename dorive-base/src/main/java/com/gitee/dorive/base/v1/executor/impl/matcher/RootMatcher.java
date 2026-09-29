/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.executor.impl.matcher;

import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import com.gitee.dorive.base.v1.executor.api.Matcher;

public class RootMatcher implements Matcher {
    @Override
    public boolean matches(RepositoryItem repositoryItem) {
        return repositoryItem.isRoot();
    }
}
