/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.executor.entity.eop;

import com.gitee.dorive.base.v1.executor.entity.op.Operation;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EntityOp extends Operation {

    private List<?> entities;

    public EntityOp(List<?> entities) {
        this.entities = entities;
    }

}
