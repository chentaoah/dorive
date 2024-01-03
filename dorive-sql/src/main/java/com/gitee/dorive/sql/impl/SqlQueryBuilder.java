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

package com.gitee.dorive.sql.impl;

import cn.hutool.core.collection.CollUtil;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.OrderBy;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.core.util.ExampleUtils;
import com.gitee.dorive.query.api.QueryBuilder;
import com.gitee.dorive.query.entity.BuildQuery;
import com.gitee.dorive.sql.api.SqlRunner;
import com.gitee.dorive.sql.entity.ArgSegment;
import com.gitee.dorive.sql.entity.SelectSegment;
import com.gitee.dorive.sql.entity.TableSegment;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class SqlQueryBuilder implements QueryBuilder {

    private SegmentBuilder segmentBuilder;
    private SqlRunner sqlRunner;

    @Override
    public void buildQuery(Context context, BuildQuery buildQuery) {
        Example example = buildQuery.getExample();
        boolean onlyCount = buildQuery.isOnlyCount();
        OrderBy orderBy = example.getOrderBy();

        example = ExampleUtils.clone(example);
        buildQuery.setExample(example);
        Page<Object> page = example.getPage();

        SelectSegment selectSegment = segmentBuilder.buildSegment(context, buildQuery);
        char letter = selectSegment.getLetter();
        TableSegment tableSegment = selectSegment.getTableSegment();
        List<ArgSegment> argSegments = selectSegment.getArgSegments();
        List<Object> args = selectSegment.getArgs();

        if (!tableSegment.isJoin() || argSegments.isEmpty()) {
            return;
        }

        String tableAlias = tableSegment.getTableAlias();

        selectSegment.setDistinct(true);
        List<String> selectColumns = new ArrayList<>(2);
        selectColumns.add(tableAlias + ".id");
        selectSegment.setSelectColumns(selectColumns);

        String selectSql = selectSegment.selectSql();
        String fromWhereSql = selectSegment.fromWhereSql();

        if (onlyCount) {
            String countSql = selectSql + fromWhereSql;
            long count = sqlRunner.selectCount("SELECT COUNT(*) AS total FROM (" + countSql + ") " + letter, args.toArray());
            if (page == null) {
                page = new Page<>();
                example.setPage(page);
            }
            page.setTotal(count);
            buildQuery.setCountQueried(true);
            return;
        }

        if (page != null) {
            String countSql = selectSql + fromWhereSql;
            long count = sqlRunner.selectCount("SELECT COUNT(*) AS total FROM (" + countSql + ") " + letter, args.toArray());
            page.setTotal(count);
            buildQuery.setCountQueried(true);
            if (count == 0L) {
                buildQuery.setAbandoned(true);
                return;
            }
        }

        boolean rebuildSql = false;
        if (orderBy != null) {
            for (String property : orderBy.getProperties()) {
                if (!"id".equals(property)) {
                    selectColumns.add(tableAlias + "." + property);
                    rebuildSql = true;
                }
            }
            selectSegment.setOrderBy(orderBy.toString());
        }
        if (rebuildSql) {
            selectSql = selectSegment.selectSql();
        }

        if (page != null) {
            selectSegment.setLimit(page.toString());
        }

        String sql = selectSql + fromWhereSql + selectSegment.lastSql();
        List<Map<String, Object>> resultMaps = sqlRunner.selectList(sql, args.toArray());
        List<Object> primaryKeys = CollUtil.map(resultMaps, map -> map.get("id"), true);
        if (!primaryKeys.isEmpty()) {
            example.in("id", primaryKeys);
            buildQuery.setDataSetQueried(true);
        } else {
            buildQuery.setAbandoned(true);
        }
    }

}
