/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.repository.v1.api;

import com.gitee.dorive.base.v1.binder.api.BinderExecutor;
import com.gitee.dorive.base.v1.definition.entity.EntityElement;
import com.gitee.dorive.base.v1.executor.api.Executor;
import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import com.gitee.dorive.base.v1.repository.api.RepositoryEle;

public interface RepositoryContextBuilder {

    void prepare(RepositoryContext repositoryContext);

    void determineEnableEventPublish(RepositoryContext repositoryContext);

    RepositoryEle newRepositoryEle(RepositoryContext repositoryContext, EntityElement entityElement);

    BinderExecutor newBinderExecutor(RepositoryContext repositoryContext, EntityElement entityElement);

    Executor newExecutor(RepositoryContext repositoryContext);

    void initialize(RepositoryContext repositoryContext);

}
