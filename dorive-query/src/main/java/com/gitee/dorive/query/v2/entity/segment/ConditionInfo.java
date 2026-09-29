/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.query.v2.entity.segment;

import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import lombok.Data;

@Data
public class ConditionInfo {
    private RepositoryContext source;
    private String sourceField;
    private RepositoryContext target;
    private String targetField;
    private String literal;
}
