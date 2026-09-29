/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.mybatis.v2.entity;

import cn.hutool.core.util.StrUtil;
import cn.hutool.db.sql.SqlBuilder;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class SelectSegment {

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
