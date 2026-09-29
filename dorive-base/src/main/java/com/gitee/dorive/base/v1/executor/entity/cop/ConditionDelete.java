/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.executor.entity.cop;

import com.gitee.dorive.base.v1.executor.entity.qry.Example;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConditionDelete extends Condition {

    public ConditionDelete(Object primaryKey) {
        super(primaryKey);
    }

    public ConditionDelete(Example example) {
        super(example);
    }

}
