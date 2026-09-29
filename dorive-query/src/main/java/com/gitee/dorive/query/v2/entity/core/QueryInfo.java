/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.query.v2.entity.core;

import com.gitee.dorive.query.v2.impl.core.ExampleResolver;
import lombok.Data;

import java.util.List;

@Data
public class QueryInfo {
    private List<QueryRepositoryMapping> queryRepositoryMappings;
    private List<QueryRepositoryMapping> reversedQueryRepositoryMappings;
    private ExampleResolver exampleResolver;
}
