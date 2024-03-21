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

package com.gitee.dorive.sql.entity.segment;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
@AllArgsConstructor
public class OnSegment {

    private String tableAlias;
    private String column;
    private String joinTableAlias;
    private String joinColumn;
    private String literal;

    public OnSegment(String tableAlias, String column, String joinTableAlias, String joinColumn) {
        this.tableAlias = tableAlias;
        this.column = column;
        this.joinTableAlias = joinTableAlias;
        this.joinColumn = joinColumn;
    }

    public OnSegment(String joinTableAlias, String joinColumn, String literal) {
        this.joinTableAlias = joinTableAlias;
        this.joinColumn = joinColumn;
        this.literal = literal;
    }

    @Override
    public String toString() {
        if (StringUtils.isNotBlank(tableAlias) && StringUtils.isNotBlank(column)) {
            return tableAlias + "." + column + " = " + joinTableAlias + "." + joinColumn;

        } else if (StringUtils.isNotBlank(literal)) {
            return joinTableAlias + "." + joinColumn + " = " + literal;
        }
        return tableAlias + "." + column + " = " + joinTableAlias + "." + joinColumn;
    }

}
