/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.executor.entity.cop;

import com.gitee.dorive.base.v1.executor.entity.op.Operation;
import com.gitee.dorive.base.v1.executor.entity.qry.Example;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Condition extends Operation {

    private Object primaryKey;
    private Example example;

    public Condition(Object primaryKey) {
        this.primaryKey = primaryKey;
    }

    public Condition(Example example) {
        this.example = example;
    }

    public boolean isEmpty() {
        return primaryKey == null && example == null;
    }

}
