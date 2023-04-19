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

package com.gitee.dorive.spring.boot.starter.entity;

import cn.hutool.db.sql.SqlBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class SelectSegment extends Segment {

    private boolean distinct;
    private List<String> columns;
    private String tableName;
    private String tableAlias;
    private List<JoinSegment> joinSegments;
    private List<Argument> arguments;
    private String groupBy;
    private String orderBy;
    private String limit;

    @Override
    public String toString() {
        SqlBuilder sqlBuilder = SqlBuilder.create();
        sqlBuilder.select(distinct, columns);
        sqlBuilder.from(tableName + " " + tableAlias);
        return sqlBuilder.toString();
    }

}
