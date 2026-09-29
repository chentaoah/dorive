/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.executor.entity.cop;

import com.gitee.dorive.base.v1.executor.entity.qry.Example;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.Set;

@Getter
@Setter
public class ConditionUpdate extends Condition {

    private Object entity;
    private Set<String> nullableProps = Collections.emptySet();

    public ConditionUpdate(Object entity, Object primaryKey) {
        super(primaryKey);
        this.entity = entity;
    }

    public ConditionUpdate(Object entity, Example example) {
        super(example);
        this.entity = entity;
    }

}
