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
import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.executor.Executor;
import com.gitee.dorive.core.entity.common.EntityStoreInfo;
import com.gitee.dorive.core.entity.executor.Criterion;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.OrderBy;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.entity.executor.UnionExample;
import com.gitee.dorive.core.entity.operation.cop.Query;
import com.gitee.dorive.core.impl.executor.AbstractProxyExecutor;
import com.gitee.dorive.sql.api.SqlRunner;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class UnionExecutor extends AbstractProxyExecutor {

    private SqlRunner sqlRunner;
    private EntityStoreInfo entityStoreInfo;

    public UnionExecutor(Executor executor, SqlRunner sqlRunner, EntityStoreInfo entityStoreInfo) {
        super(executor);
        this.sqlRunner = sqlRunner;
        this.entityStoreInfo = entityStoreInfo;
    }

    @Override
    public Result<Object> executeQuery(Context context, Query query) {
        Example example = query.getExample();
        if (example instanceof UnionExample) {
            UnionExample unionExample = (UnionExample) example;
            String sql = buildSql(unionExample);
            if (StringUtils.isNotBlank(sql)) {
                List<Map<String, Object>> resultMaps = sqlRunner.selectList(sql, (Object) null);
                return buildResult(resultMaps);
            }
        }
        return super.executeQuery(context, query);
    }

    private String buildSql(UnionExample unionExample) {
        if (unionExample.isEmpty()) {
            return null;
        }
        String selectColumns = buildSelectColumns(unionExample);
        String lastSql = buildLastSql(unionExample);
        List<String> sqls = buildSqls(unionExample, selectColumns, lastSql);
        return StrUtil.join(" UNION ALL ", sqls);
    }

    private String buildSelectColumns(Example example) {
        String selectColumns = entityStoreInfo.getSelectColumns();
        List<String> selectProps = example.getSelectProps();
        if (selectProps != null && !selectProps.isEmpty()) {
            selectColumns = StrUtil.join(",", selectProps);
        }
        return selectColumns;
    }

    private String buildLastSql(Example example) {
        StringBuilder lastSql = new StringBuilder();
        OrderBy orderBy = example.getOrderBy();
        if (orderBy != null) {
            lastSql.append(" ").append(orderBy);
        }
        Page<Object> page = example.getPage();
        if (page != null) {
            lastSql.append(" ").append(page);
        }
        return lastSql.toString();
    }

    private List<String> buildSqls(UnionExample unionExample, String selectColumns, String lastSql) {
        List<Example> examples = unionExample.getExamples();
        List<String> sqls = new ArrayList<>(examples.size());
        Example example = examples.get(0);
        String sql = doBuildSql(false, selectColumns, example, lastSql);
        sqls.add(sql);
        for (int index = 1; index < examples.size(); index++) {
            Example nextExample = examples.get(index);
            String nextSql = doBuildSql(true, selectColumns, nextExample, lastSql);
            sqls.add(nextSql);
        }
        return sqls;
    }

    private String doBuildSql(boolean hasBrackets, String selectColumns, Example example, String lastSql) {
        StringBuilder sql = new StringBuilder();
        if (hasBrackets) {
            sql.append("(");
        }
        String template = "SELECT %s,%s FROM %s WHERE %s";
        String selectSuffix = example.getSelectSuffix();
        String tableName = entityStoreInfo.getTableName();
        String criteria = CollUtil.join(example.getCriteria(), " AND ", Criterion::toString);
        String selectSql = String.format(template, selectColumns, selectSuffix, tableName, criteria);
        sql.append(selectSql);
        sql.append(lastSql);
        if (hasBrackets) {
            sql.append(")");
        }
        return sql.toString();
    }

    @SuppressWarnings("unchecked")
    private Result<Object> buildResult(List<Map<String, Object>> resultMaps) {
        Map<String, Map<String, Object>> idResultMapMapping = new LinkedHashMap<>(resultMaps.size() * 4 / 3 + 1);
        for (Map<String, Object> resultMap : resultMaps) {
            Object id = resultMap.get(entityStoreInfo.getIdColumn());
            Object row = resultMap.get("$row");
            if (id == null || row == null) {
                continue;
            }
            String idStr = id.toString();
            String rowStr = row.toString();
            if (!idResultMapMapping.containsKey(idStr)) {
                idResultMapMapping.put(idStr, resultMap);
            } else {
                Map<String, Object> existResultMap = idResultMapMapping.get(idStr);
                List<String> existRows = (List<String>) existResultMap.computeIfAbsent("$rows", key -> new ArrayList<>(4));
                if (existRows.isEmpty()) {
                    Object existRow = existResultMap.get("$row");
                    if (existRow != null) {
                        existRows.add(existRow.toString());
                    }
                }
                existRows.add(rowStr);
            }
        }
        return new Result<>(null, new ArrayList<>(idResultMapMapping.values()));
    }

}
