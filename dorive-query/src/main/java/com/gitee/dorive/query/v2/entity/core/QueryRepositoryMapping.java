/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.query.v2.entity.core;

import com.gitee.dorive.base.v1.definition.constant.Operator;
import com.gitee.dorive.base.v1.definition.constant.OperatorV2;
import com.gitee.dorive.base.v1.definition.def.QueryFieldDef;
import com.gitee.dorive.base.v1.definition.entity.QueryFieldDefinition;
import com.gitee.dorive.base.v1.executor.entity.qry.Criterion;
import com.gitee.dorive.base.v1.executor.entity.qry.Example;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class QueryRepositoryMapping {
    private RepositoryInfo repositoryInfo;
    private List<QueryFieldDefinition> queryFields;

    public void appendCriteria(Object query, Example example) {
        if (queryFields == null || queryFields.isEmpty()) {
            return;
        }
        for (QueryFieldDefinition queryField : queryFields) {
            Object fieldValue = queryField.getFieldValue(query);
            if (fieldValue != null) {
                QueryFieldDef queryFieldDef = queryField.getQueryFieldDef();
                String fieldName = queryFieldDef.getField();
                String operator = queryFieldDef.getOperator();
                if (OperatorV2.NULL_SWITCH.equals(operator) && fieldValue instanceof Boolean) {
                    operator = (Boolean) fieldValue ? Operator.IS_NULL : Operator.IS_NOT_NULL;
                    fieldValue = null;
                }
                example.getCriteria().add(new Criterion(fieldName, operator, fieldValue));
            }
        }
    }
}
