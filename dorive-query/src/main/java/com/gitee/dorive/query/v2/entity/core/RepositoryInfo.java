/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.query.v2.entity.core;

import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RepositoryInfo {
    private RepositoryInfo parent;
    private String lastAccessPath;
    private RepositoryItem lastRepositoryItem;
    private String absolutePath;
    private RepositoryContext repositoryContext;
    private Integer sequence;
    private List<RepositoryInfo> children;
}
