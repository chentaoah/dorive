/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.repository.api;

import com.gitee.dorive.base.v1.binder.api.BinderExecutor;
import com.gitee.dorive.base.v1.executor.api.Options;
import com.gitee.dorive.base.v1.executor.entity.qry.Example;

import java.util.List;

public interface RepositoryItem extends RepositoryEle {

    RepositoryContext getRepositoryContext();

    String getName();

    boolean isCollection();

    String getAccessPath();

    boolean isRoot();

    boolean isAggregated();

    BinderExecutor getBinderExecutor();

    List<Object> selectByExample(Options options, Example example);

    long selectCountByExample(Options options, Example example);

}
