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

package com.gitee.dorive.sql.impl.executor;

import cn.hutool.core.collection.CollUtil;
import com.gitee.dorive.api.entity.ele.EntityElement;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.entity.common.EntityStoreInfo;
import com.gitee.dorive.core.entity.executor.*;
import com.gitee.dorive.query.entity.QueryContext;
import com.gitee.dorive.query.entity.enums.ResultType;
import com.gitee.dorive.query.impl.executor.AbstractQueryExecutor;
import com.gitee.dorive.query.repository.AbstractQueryRepository;
import com.gitee.dorive.sql.api.SqlRunner;
import com.gitee.dorive.sql.entity.segment.ArgSegment;
import com.gitee.dorive.sql.entity.segment.SelectSegment;
import com.gitee.dorive.sql.entity.segment.TableSegment;
import com.gitee.dorive.sql.impl.segment.SegmentBuilder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class SqlQueryExecutor extends AbstractQueryExecutor {

    private SqlRunner sqlRunner;

    public SqlQueryExecutor(AbstractQueryRepository<?, ?> repository, SqlRunner sqlRunner) {
        super(repository);
        this.sqlRunner = sqlRunner;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result<Object> executeQuery(QueryContext queryContext, QueryWrapper queryWrapper) {
        Context context = queryContext.getContext();
        ResultType resultType = queryContext.getResultType();
        Example example = queryContext.getExample();

        EntityElement entityElement = repository.getEntityElement();
        Map<String, Object> attributes = entityElement.getAttributes();
        EntityStoreInfo entityStoreInfo = (EntityStoreInfo) attributes.get(EntityStoreInfo.class.getName());
        String idColumn = entityStoreInfo.getIdColumn();

        OrderBy orderBy = example.getOrderBy();
        Page<Object> page = example.getPage();

        boolean needCount = queryContext.isNeedCount();
        Result<Object> emptyResult = queryContext.newEmptyResult();

        SegmentBuilder segmentBuilder = new SegmentBuilder(queryContext);
        SelectSegment selectSegment = segmentBuilder.buildSegment(context, null);
        char letter = selectSegment.getLetter();
        TableSegment tableSegment = selectSegment.getTableSegment();
        List<ArgSegment> argSegments = selectSegment.getArgSegments();
        List<Object> args = selectSegment.getArgs();
        if (!tableSegment.isJoin() || argSegments.isEmpty()) {
            return super.executeQuery(queryContext, queryWrapper);
        }

        String tableAlias = tableSegment.getTableAlias();
        selectSegment.setDistinct(true);
        List<String> selectColumns = new ArrayList<>(2);
        selectColumns.add(tableAlias + "." + idColumn);
        selectSegment.setSelectColumns(selectColumns);

        String selectSql = selectSegment.selectSql();
        String fromWhereSql = selectSegment.fromWhereSql();

        if (needCount) {
            String countSql = selectSql + fromWhereSql;
            long count = sqlRunner.selectCount("SELECT COUNT(*) AS total FROM (" + countSql + ") " + letter, args.toArray());
            if (count == 0L) {
                return emptyResult;
            }
            if (resultType == ResultType.COUNT) {
                return new Result<>(count);

            } else if (page != null) {
                page.setTotal(count);
            }
        }

        boolean rebuildSql = false;
        if (orderBy != null) {
            for (String property : orderBy.getProperties()) {
                if (!idColumn.equals(property)) {
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
        List<Object> primaryKeys = CollUtil.map(resultMaps, map -> map.get(idColumn), true);
        if (!primaryKeys.isEmpty()) {
            Example newExample = new InnerExample().in(entityElement.getPrimaryKey(), primaryKeys);
            List<Object> entities = (List<Object>) repository.selectByExample(context, newExample);
            if (page != null) {
                page.setRecords(entities);
                return new Result<>(page);
            } else {
                return new Result<>(entities);
            }
        }
        return emptyResult;
    }

}
