/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.definition.constant;

public interface Operator {
    String EQ = "=";
    String NE = "<>";
    String GT = ">";
    String GE = ">=";
    String LT = "<";
    String LE = "<=";
    String IN = "IN";
    String NOT_IN = "NOT IN";
    String LIKE = "LIKE";
    String NOT_LIKE = "NOT LIKE";
    String IS_NULL = "IS NULL";
    String IS_NOT_NULL = "IS NOT NULL";
    String MULTI_IN = "MULTI_IN";
    String MULTI_NOT_IN = "MULTI_NOT_IN";
    String AND = "AND";
    String OR = "OR";
}
