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

import cn.hutool.core.util.StrUtil;
import cn.hutool.db.sql.SqlBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
public class SelectSegment extends Segment {

    private boolean distinct;
    private List<String> columns;
    private String tableName;
    private String tableAlias;
    private List<JoinSegment> joinSegments;
    private List<ArgSegment> argSegments;
    private String groupBy;
    private String orderBy;
    private String limit;

    public String selectSql() {
        SqlBuilder sqlBuilder = SqlBuilder.create();
        sqlBuilder.select(distinct, columns);
        return sqlBuilder.toString();
    }

    public String fromWhereSql() {
        SqlBuilder sqlBuilder = SqlBuilder.create();
        sqlBuilder.from(tableName + " " + tableAlias);
        for (JoinSegment joinSegment : joinSegments) {
            if (joinSegment.isAvailable()) {
                sqlBuilder.join(joinSegment.getTableName() + " " + joinSegment.getTableAlias(), SqlBuilder.Join.LEFT);
                List<OnSegment> onSegments = joinSegment.getOnSegments().stream()
                        .filter(onSegment -> onSegment.getDirectedSegments().get(0).isAvailable())
                        .collect(Collectors.toList());
                sqlBuilder.on(StrUtil.join(" AND ", onSegments));
            }
        }
        sqlBuilder.where(StrUtil.join(" AND ", argSegments));
        return sqlBuilder.toString();
    }

    public String lastSql() {
        SqlBuilder sqlBuilder = SqlBuilder.create();
        if (groupBy != null) {
            sqlBuilder.append(" ").append(groupBy);
        }
        if (orderBy != null) {
            sqlBuilder.append(" ").append(orderBy);
        }
        if (limit != null) {
            sqlBuilder.append(" ").append(limit);
        }
        return sqlBuilder.toString();
    }

    @Override
    public String toString() {
        return selectSql() + fromWhereSql() + lastSql();
    }

}
