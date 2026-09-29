/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.definition.entity;

import com.gitee.dorive.base.v1.definition.def.QueryFieldDef;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QueryFieldDefinition extends Field {
    private QueryFieldDef queryFieldDef;

    public QueryFieldDefinition(java.lang.reflect.Field field) {
        super(field);
    }
}
