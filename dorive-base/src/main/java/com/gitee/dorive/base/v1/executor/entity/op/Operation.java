/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.executor.entity.op;

import lombok.Data;

/**
 * 操作
 */
@Data
public class Operation {
    private boolean root;
    private Boolean matched;

    public boolean isMatched() {
        return matched != null && matched;
    }

    public boolean isNotMatched() {
        return matched != null && !matched;
    }
}
