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
import com.gitee.dorive.query.entity.QueryContext;
import com.gitee.dorive.query.entity.enums.ResultType;
import com.gitee.dorive.query.repository.AbstractQueryRepository;
import com.gitee.dorive.sql.api.CountQuerier;
import com.gitee.dorive.sql.api.SqlRunner;
import com.gitee.dorive.sql.entity.common.CountQuery;
import com.gitee.dorive.sql.entity.common.SegmentUnit;
import com.gitee.dorive.sql.entity.segment.SelectSegment;
import com.gitee.dorive.sql.impl.segment.SelectSegmentBuilder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class CountSqlQueryExecutor extends SqlQueryExecutor implements CountQuerier {

    public CountSqlQueryExecutor(AbstractQueryRepository<?, ?> repository, SqlRunner sqlRunner) {
        super(repository, sqlRunner);
    }

    @Override
    public Map<String, Long> selectCountMap(Context context, CountQuery countQuery) {
        QueryContext queryContext = new QueryContext(context, countQuery.getQuery(), ResultType.COUNT);
        resolve(queryContext);
        SegmentUnit segmentUnit = (SegmentUnit) queryContext.getQueryUnit();
        EntityElement entityElement = segmentUnit.getEntityElement();
        String tableAlias = segmentUnit.getTableAlias();

        SelectSegmentBuilder selectSegmentBuilder = new SelectSegmentBuilder(queryContext);
        List<SegmentUnit> segmentUnits = selectSegmentBuilder.select(countQuery.getSelector());
        SelectSegment selectSegment = selectSegmentBuilder.build();
        List<Object> args = selectSegment.getArgs();

        // group by
        List<String> groupBy = entityElement.toAliases(countQuery.getGroupBy());
        String groupByColumns = CollUtil.join(groupBy, ",", tableAlias + ".", null);
        selectSegment.setGroupBy("GROUP BY " + groupByColumns);

        // count by
        SegmentUnit selectSegmentUnit = segmentUnits != null && !segmentUnits.isEmpty() ? segmentUnits.get(0) : segmentUnit;
        String countByExp = buildCountByExp(countQuery, selectSegmentUnit);

        // select columns
        List<String> selectColumns = new ArrayList<>(2);
        selectColumns.add(groupByColumns);
        selectColumns.add(String.format("COUNT(%s) AS total", countByExp));
        selectSegment.setSelectColumns(selectColumns);

        List<Map<String, Object>> resultMaps = sqlRunner.selectList(selectSegment.toString(), args.toArray());
        Map<String, Long> countMap = new LinkedHashMap<>(resultMaps.size() * 4 / 3 + 1);
        resultMaps.forEach(resultMap -> countMap.put(buildKey(resultMap, groupBy), (Long) resultMap.get("total")));
        return countMap;
    }

    private String buildCountByExp(CountQuery countQuery, SegmentUnit segmentUnit) {
        EntityElement entityElement = segmentUnit.getEntityElement();
        String tableAlias = segmentUnit.getTableAlias();

        List<String> countBy = entityElement.toAliases(countQuery.getCountBy());
        String countByStr = CollUtil.join(countBy, ",',',", tableAlias + ".", null);

        StringBuilder countByExp = new StringBuilder();
        if (countQuery.isDistinct()) {
            countByExp.append("DISTINCT ");
        }
        if (countBy.size() == 1) {
            countByExp.append(countByStr);

        } else if (countBy.size() > 1) {
            countByExp.append("CONCAT(").append(countByStr).append(")");
        }
        return countByExp.toString();
    }

    private String buildKey(Map<String, Object> resultMap, List<String> groupBy) {
        StringBuilder keyBuilder = new StringBuilder();
        for (String column : groupBy) {
            String valueStr = String.valueOf(resultMap.get(column)).trim();
            keyBuilder.append(valueStr).append(", ");
        }
        int length = keyBuilder.length();
        if (length > 0) {
            keyBuilder.delete(length - 2, length);
        }
        return keyBuilder.toString();
    }

}
