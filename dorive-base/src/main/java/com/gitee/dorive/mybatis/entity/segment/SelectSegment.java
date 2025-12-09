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

package com.gitee.dorive.mybatis.entity.segment;

import cn.hutool.core.util.StrUtil;
import cn.hutool.db.sql.SqlBuilder;
import com.gitee.dorive.mybatis.api.sql.Segment;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class SelectSegment implements Segment {

    private boolean distinct;
    private List<String> selectColumns = Collections.emptyList();
    private TableSegment tableSegment;
    private List<TableJoinSegment> tableJoinSegments = new ArrayList<>(6);
    private List<ArgSegment> argSegments = new ArrayList<>(8);
    private List<Object> args = new ArrayList<>(8);
    private String groupBy;
    private String orderBy;
    private String limit;

    public String selectSql() {
        SqlBuilder sqlBuilder = SqlBuilder.create();
        sqlBuilder.select(distinct, selectColumns);
        return sqlBuilder.toString();
    }

    public String fromWhereSql() {
        SqlBuilder sqlBuilder = SqlBuilder.create();
        sqlBuilder.from(tableSegment.toString());
        for (TableJoinSegment tableJoinSegment : tableJoinSegments) {
            sqlBuilder.join(tableJoinSegment.toString(), SqlBuilder.Join.LEFT);
            sqlBuilder.on(StrUtil.join(" AND ", tableJoinSegment.getOnSegments()));
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
