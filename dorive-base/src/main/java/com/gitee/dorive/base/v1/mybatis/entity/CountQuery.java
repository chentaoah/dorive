/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.mybatis.entity;

import com.gitee.dorive.base.v1.executor.api.Matcher;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CountQuery {
    private Object query;
    private boolean distinct = true;
    private Matcher matcher;
    private List<String> countBy;
    private List<String> groupBy;

    public CountQuery(Object query, String countBy, String groupBy) {
        this.query = query;
        this.countBy = Collections.singletonList(countBy);
        this.groupBy = Collections.singletonList(groupBy);
    }
}
