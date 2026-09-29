/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.mybatis.v2.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArgSegment extends ExprSegment {

    public ArgSegment(String leftExpr, String operator, String rightExpr) {
        super(leftExpr, operator, rightExpr);
    }

}
