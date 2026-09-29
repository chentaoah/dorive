/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.definition.entity;

import com.gitee.dorive.base.v1.definition.def.QueryDef;
import lombok.Data;

import java.util.List;

@Data
public class QueryDefinition {
    private QueryDef queryDef;
    private Class<?> genericType;
    private List<QueryFieldDefinition> queryFieldDefinitions;
    private List<Field> ignoreFields;
    private Field sortByField;
    private Field orderField;
    private Field pageField;
    private Field limitField;
}
