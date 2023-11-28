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

package com.gitee.dorive.sql.entity;

import cn.hutool.core.util.StrUtil;
import cn.hutool.db.sql.SqlBuilder;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class SelectSegment {

    private char letter = 'a';
    private boolean distinct;
    private List<String> columns = Collections.emptyList();
    private TableSegment tableSegment;
    private List<JoinSegment> joinSegments = new ArrayList<>();
    private List<ArgSegment> argSegments = new ArrayList<>();
    private List<Object> args = new ArrayList<>();
    private String groupBy;
    private String orderBy;
    private String limit;

    public String generateTableAlias() {
        String tableAlias = String.valueOf(letter);
        letter = (char) (letter + 1);
        return tableAlias;
    }

    public void filterTableSegments() {
        if (tableSegment.isJoin()) {
            argSegments.addAll(tableSegment.getArgSegments());
        }
        List<JoinSegment> newJoinSegments = new ArrayList<>(joinSegments.size());
        for (JoinSegment joinSegment : joinSegments) {
            TableSegment tableSegment = joinSegment.getTableSegment();
            if (tableSegment.isJoin()) {
                newJoinSegments.add(joinSegment);
                argSegments.addAll(tableSegment.getArgSegments());
            }
        }
        joinSegments = newJoinSegments;
    }

    public String selectSql() {
        SqlBuilder sqlBuilder = SqlBuilder.create();
        sqlBuilder.select(distinct, columns);
        return sqlBuilder.toString();
    }

    public String fromWhereSql() {
        SqlBuilder sqlBuilder = SqlBuilder.create();
        sqlBuilder.from(tableSegment.toString());
        for (JoinSegment joinSegment : joinSegments) {
            TableSegment tableSegment = joinSegment.getTableSegment();
            sqlBuilder.join(tableSegment.toString(), SqlBuilder.Join.LEFT);
            sqlBuilder.on(StrUtil.join(" AND ", joinSegment.getOnSegments()));
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
