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
import com.gitee.dorive.sql.api.SqlHelper;
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
    private SqlHelper sqlHelper;

    @Override
    public BuildQuery build(Context context, Object query) {
        BuildQuery buildQuery = (BuildQuery) query;
        Example example = buildQuery.getExample();
        OrderBy orderBy = example.getOrderBy();

        example = ExampleUtils.tryClone(example);
        buildQuery.setExample(example);
        Page<Object> page = example.getPage();

        SelectSegment selectSegment = segmentBuilder.buildSegment(context, buildQuery);
        char letter = selectSegment.getLetter();
        TableSegment tableSegment = selectSegment.getTableSegment();
        List<ArgSegment> argSegments = selectSegment.getArgSegments();
        List<Object> args = selectSegment.getArgs();

        if (!tableSegment.isJoin() || argSegments.isEmpty()) {
            return buildQuery;
        }

        selectSegment.setDistinct(true);

        List<String> selectColumns = new ArrayList<>(2);
        String tableAlias = tableSegment.getTableAlias();
        selectColumns.add(tableAlias + ".id");
        selectSegment.setColumns(selectColumns);

        String fromWhereSql = selectSegment.fromWhereSql();

        if (page != null) {
            String countSql = selectSegment.selectSql() + fromWhereSql;
            long count = sqlHelper.selectCount("SELECT COUNT(*) AS total FROM (" + countSql + ") " + letter, args.toArray());
            page.setTotal(count);
            buildQuery.setPageQueried(true);
            if (count == 0L) {
                buildQuery.setAbandoned(true);
                return buildQuery;
            }
        }

        if (orderBy != null) {
            for (String property : orderBy.getProperties()) {
                if (!"id".equals(property)) {
                    selectColumns.add(tableAlias + "." + property);
                }
            }
            selectSegment.setOrderBy(orderBy.toString());
        }
        if (page != null) {
            selectSegment.setLimit(page.toString());
        }

        String selectSql = selectSegment.selectSql() + fromWhereSql + selectSegment.lastSql();
        List<Map<String, Object>> resultMaps = sqlHelper.selectList(selectSql, args.toArray());
        List<Object> primaryKeys = CollUtil.map(resultMaps, map -> map.get("id"), true);
        if (!primaryKeys.isEmpty()) {
            example.eq("id", primaryKeys);
        } else {
            buildQuery.setAbandoned(true);
        }

        return buildQuery;
    }

}
