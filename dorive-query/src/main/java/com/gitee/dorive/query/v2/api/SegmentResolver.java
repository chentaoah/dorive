/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.query.v2.api;

import com.gitee.dorive.base.v1.executor.entity.qry.Example;
import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import com.gitee.dorive.query.v2.entity.segment.JoinInfo;

import java.util.List;
import java.util.Map;

public interface SegmentResolver {

    Object resolve(Map<RepositoryContext, String> repositoryAliasMap,
                   List<JoinInfo> joinInfos,
                   Map<RepositoryContext, Example> repositoryExampleMap,
                   RepositoryContext repositoryContext,
                   Example example);

}
