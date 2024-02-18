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
import com.gitee.dorive.core.entity.operation.Query;
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

    private EntityStoreInfo entityStoreInfo;
    private SqlRunner sqlRunner;

    public UnionExecutor(Executor executor, EntityStoreInfo entityStoreInfo, SqlRunner sqlRunner) {
        super(executor);
        this.entityStoreInfo = entityStoreInfo;
        this.sqlRunner = sqlRunner;
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
        List<Example> examples = unionExample.getExamples();
        if (examples.isEmpty()) {
            return null;
        }
        String selectColumns = entityStoreInfo.getSelectColumns();
        List<String> selectProps = unionExample.getSelectProps();
        if (selectProps != null && !selectProps.isEmpty()) {
            selectColumns = StrUtil.join(",", selectProps);
        }
        List<String> sqls = new ArrayList<>(examples.size());
        Example example = examples.get(0);
        String sql = buildSql(false, selectColumns, example);
        sqls.add(sql);
        for (int index = 1; index < examples.size(); index++) {
            Example nextExample = examples.get(index);
            String nextSql = buildSql(true, selectColumns, nextExample);
            sqls.add(nextSql);
        }
        return StrUtil.join(" UNION ALL ", sqls);
    }

    private String buildSql(boolean hasBrackets, String selectColumns, Example example) {
        StringBuilder sqlBuilder = new StringBuilder();
        if (hasBrackets) {
            sqlBuilder.append("(");
        }
        String template = "SELECT %s,%s FROM %s WHERE %s";
        String selectSuffix = example.getSelectSuffix();
        String tableName = entityStoreInfo.getTableName();
        String criteria = CollUtil.join(example.getCriteria(), " AND ", Criterion::toString);
        String sql = String.format(template, selectColumns, selectSuffix, tableName, criteria);
        sqlBuilder.append(sql);
        OrderBy orderBy = example.getOrderBy();
        if (orderBy != null) {
            sqlBuilder.append(" ").append(orderBy);
        }
        Page<Object> page = example.getPage();
        if (page != null) {
            sqlBuilder.append(" ").append(page);
        }
        if (hasBrackets) {
            sqlBuilder.append(")");
        }
        return sqlBuilder.toString();
    }

    @SuppressWarnings("unchecked")
    private Result<Object> buildResult(List<Map<String, Object>> resultMaps) {
        Map<String, Map<String, Object>> idResultMapMapping = new LinkedHashMap<>(resultMaps.size() * 4 / 3 + 1);
        for (Map<String, Object> resultMap : resultMaps) {
            Object id = resultMap.get("id");
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
