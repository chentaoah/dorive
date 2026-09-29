/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.mybatis.v2.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TableSegment {

    private String tableName;
    private String tableAlias;
    private List<ArgSegment> argSegments;

    @Override
    public String toString() {
        return tableName + " " + tableAlias;
    }

}
