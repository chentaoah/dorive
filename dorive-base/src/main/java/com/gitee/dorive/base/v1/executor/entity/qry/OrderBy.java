/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.executor.entity.qry;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class OrderBy {

    private List<String> properties;
    private String sort;

    @Override
    public String toString() {
        return "ORDER BY " + StrUtil.join(",", properties) + " " + sort.toUpperCase();
    }

}
