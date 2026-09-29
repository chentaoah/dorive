/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.executor.entity.qry;

import com.gitee.dorive.base.v1.executor.impl.util.CriterionUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Criterion {

    private String property;
    private String operator;
    private Object value;

    public Criterion(String property, String operator) {
        this.property = property;
        this.operator = operator;
    }

    @Override
    public String toString() {
        return CriterionUtils.toString(this);
    }

}
