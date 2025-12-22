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

package com.gitee.dorive.mybatis2.v1.impl.segment;

import cn.hutool.core.collection.CollUtil;
import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.base.v1.core.entity.cop.Query;
import com.gitee.dorive.base.v1.core.entity.op.Result;
import com.gitee.dorive.base.v1.core.entity.qry.Example;
import com.gitee.dorive.base.v1.core.entity.qry.OrderBy;
import com.gitee.dorive.base.v1.core.entity.qry.Page;
import com.gitee.dorive.base.v1.executor.api.Executor;
import com.gitee.dorive.base.v1.mybatis.api.SqlRunner;
import com.gitee.dorive.mybatis2.v1.entity.SelectSegment;
import com.gitee.dorive.mybatis2.v1.entity.TableSegment;
import com.gitee.dorive.query2.v1.api.SegmentExecutor;
import com.gitee.dorive.query2.v1.entity.executor.SegmentInfo;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class DefaultSegmentExecutor implements SegmentExecutor {

    private final String primaryKey;
    private final String primaryKeyAlias;
    private final SqlRunner sqlRunner;
    private final Executor executor;

    @Override
    public void buildSelectColumns(SegmentInfo segmentInfo) {
        SelectSegment selectSegment = (SelectSegment) segmentInfo.getSegment();
        selectSegment.setDistinct(true);
        TableSegment tableSegment = selectSegment.getTableSegment();
        String tableAlias = tableSegment.getTableAlias();
        List<String> selectColumns = new ArrayList<>(2);
        selectColumns.add(tableAlias + "." + primaryKeyAlias);
        selectSegment.setSelectColumns(selectColumns);
    }

    @Override
    public long executeCount(SegmentInfo segmentInfo) {
        SelectSegment selectSegment = (SelectSegment) segmentInfo.getSegment();
        String selectSql = selectSegment.selectSql();
        String fromWhereSql = selectSegment.fromWhereSql();
        List<Object> args = selectSegment.getArgs();
        String countSql = selectSql + fromWhereSql;
        return sqlRunner.selectCount("SELECT COUNT(*) AS total FROM (" + countSql + ") c", args.toArray());
    }

    @Override
    public void buildOrderByAndPage(SegmentInfo segmentInfo) {
        Example example = segmentInfo.getExample();
        SelectSegment selectSegment = (SelectSegment) segmentInfo.getSegment();

        OrderBy orderBy = example.getOrderBy();
        Page<Object> page = example.getPage();

        List<String> selectColumns = selectSegment.getSelectColumns();
        TableSegment tableSegment = selectSegment.getTableSegment();

        String tableAlias = tableSegment.getTableAlias();

        if (orderBy != null) {
            for (String property : orderBy.getProperties()) {
                if (!primaryKeyAlias.equals(property)) {
                    selectColumns.add(tableAlias + "." + property);
                }
            }
            selectSegment.setOrderBy(orderBy.toString());
        }
        if (page != null) {
            selectSegment.setLimit(page.toString());
        }
    }

    @Override
    public Result<Object> executeQuery(Context context, SegmentInfo segmentInfo) {
        Example example = segmentInfo.getExample();
        SelectSegment selectSegment = (SelectSegment) segmentInfo.getSegment();

        String selectSql = selectSegment.selectSql();
        String fromWhereSql = selectSegment.fromWhereSql();
        List<Object> args = selectSegment.getArgs();

        // 查询主键
        String sql = selectSql + fromWhereSql + selectSegment.lastSql();
        List<Map<String, Object>> resultMaps = sqlRunner.selectList(sql, args.toArray());
        List<Object> ids = CollUtil.map(resultMaps, map -> map.get(primaryKeyAlias), true);
        if (!ids.isEmpty()) {
            example.in(primaryKey, ids);
        }
        example.setPage(null);
        return executor.executeQuery(context, new Query(example));
    }

}
