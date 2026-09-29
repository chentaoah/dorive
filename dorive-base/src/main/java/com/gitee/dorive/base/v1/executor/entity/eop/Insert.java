/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.executor.entity.eop;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Insert extends EntityOp {

    public Insert(List<?> entities) {
        super(entities);
    }

}
