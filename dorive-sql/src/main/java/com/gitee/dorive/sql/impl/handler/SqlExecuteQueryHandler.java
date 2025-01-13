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

package com.gitee.dorive.sql.impl.handler;

import cn.hutool.core.collection.CollUtil;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.OrderBy;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.query.entity.QueryContext;
import com.gitee.dorive.query.entity.QueryUnit;
import com.gitee.dorive.query.entity.enums.ResultType;
import com.gitee.dorive.query.repository.AbstractQueryRepository;
import com.gitee.dorive.sql.api.SqlRunner;
import com.gitee.dorive.sql.entity.segment.ArgSegment;
import com.gitee.dorive.sql.entity.segment.SelectSegment;
import com.gitee.dorive.sql.entity.segment.TableSegment;
import com.gitee.dorive.sql.impl.segment.SelectSegmentBuilder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class SqlExecuteQueryHandler extends SqlBuildQueryHandler {

    private final SqlRunner sqlRunner;

    public SqlExecuteQueryHandler(AbstractQueryRepository<?, ?> repository, SqlRunner sqlRunner) {
        super(repository);
        this.sqlRunner = sqlRunner;
    }

    @Override
    protected void doHandle(QueryContext queryContext, Object query) {
        ResultType resultType = queryContext.getResultType();
        String primaryKey = queryContext.getPrimaryKey();
        String primaryKeyAlias = queryContext.getPrimaryKeyAlias();
        Example example = queryContext.getExample();
        QueryUnit queryUnit = queryContext.getQueryUnit();

        boolean needCount = queryContext.isNeedCount();

        OrderBy orderBy = example.getOrderBy();
        Page<Object> page = example.getPage();

        SelectSegmentBuilder selectSegmentBuilder = new SelectSegmentBuilder(queryContext);
        SelectSegment selectSegment = selectSegmentBuilder.build();

        TableSegment tableSegment = selectSegment.getTableSegment();
        List<ArgSegment> argSegments = selectSegment.getArgSegments();
        List<Object> args = selectSegment.getArgs();

        if (!tableSegment.isJoin() || argSegments.isEmpty()) {
            return;
        }

        String tableAlias = tableSegment.getTableAlias();
        selectSegment.setDistinct(true);
        List<String> selectColumns = new ArrayList<>(2);
        selectColumns.add(tableAlias + "." + primaryKeyAlias);
        selectSegment.setSelectColumns(selectColumns);

        String selectSql = selectSegment.selectSql();
        String fromWhereSql = selectSegment.fromWhereSql();

        if (needCount) {
            String countSql = selectSql + fromWhereSql;
            long count = sqlRunner.selectCount("SELECT COUNT(*) AS total FROM (" + countSql + ") c", args.toArray());
            if (count == 0L) {
                queryUnit.setAbandoned(true);
                return;
            }
            if (resultType == ResultType.COUNT) {
                queryContext.setResult(new Result<>(count));
                return;
            }
            if (page != null) {
                page.setTotal(count);
            }
        }

        boolean rebuildSql = false;
        if (orderBy != null) {
            for (String property : orderBy.getProperties()) {
                if (!primaryKeyAlias.equals(property)) {
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

        // 查询主键
        String sql = selectSql + fromWhereSql + selectSegment.lastSql();
        List<Map<String, Object>> resultMaps = sqlRunner.selectList(sql, args.toArray());
        List<Object> ids = CollUtil.map(resultMaps, map -> map.get(primaryKeyAlias), true);
        if (!ids.isEmpty()) {
            example.in(primaryKey, ids);
            doQuery(queryContext, ids);
        } else {
            queryContext.setAbandoned(true);
        }
    }
}
