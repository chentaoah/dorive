/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.executor.entity.eop;

import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class Update extends EntityOp {

    private Set<String> nullableProps = Collections.emptySet();

    public Update(List<?> entities) {
        super(entities);
    }

}
