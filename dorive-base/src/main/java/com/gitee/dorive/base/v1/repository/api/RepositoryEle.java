/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.repository.api;

import com.gitee.dorive.base.v1.definition.entity.EntityElement;
import com.gitee.dorive.base.v1.executor.api.OperationFactory;
import com.gitee.dorive.base.v1.executor.api.Executor;

public interface RepositoryEle extends Properties, Executor {

    EntityElement getEntityElement();

    OperationFactory getOperationFactory();

    Executor getExecutor();

    Class<?> getEntityClass();

}
