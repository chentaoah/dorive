/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.query.v2.entity.segment;

import com.gitee.dorive.base.v1.executor.entity.qry.Example;
import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import lombok.Data;

@Data
public class SegmentInfo {
    private RepositoryContext selectedRepository;
    private String selectedRepositoryAlias;
    private RepositoryContext repository;
    private Example example;
    private Object segment;
}
