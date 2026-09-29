/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.mybatis.v2.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ExprSegment {

    private String leftExpr;
    private String operator;
    private String rightExpr;

    @Override
    public String toString() {
        if (rightExpr != null) {
            return leftExpr + " " + operator + " " + rightExpr;
        } else {
            return leftExpr + " " + operator;
        }
    }

}
