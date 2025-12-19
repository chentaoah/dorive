/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gitee.dorive.query2.v1.entity;

import com.gitee.dorive.base.v1.common.constant.Operator;
import com.gitee.dorive.base.v1.common.constant.OperatorV2;
import com.gitee.dorive.base.v1.common.def.QueryFieldDef;
import com.gitee.dorive.base.v1.common.entity.QueryFieldDefinition;
import com.gitee.dorive.base.v1.core.entity.qry.Criterion;
import com.gitee.dorive.base.v1.core.entity.qry.Example;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class QueryNode {
    private RepositoryNode repositoryNode;
    private List<QueryFieldDefinition> queryFields;

    public void appendCriteria(Object query, Example example) {
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
