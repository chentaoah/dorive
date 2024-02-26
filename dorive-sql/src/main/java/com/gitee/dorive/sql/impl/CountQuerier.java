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

import com.gitee.dorive.api.entity.element.EntityEle;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.query.entity.QueryContext;
import com.gitee.dorive.query.entity.enums.ResultType;
import com.gitee.dorive.query.entity.QueryWrapper;
import com.gitee.dorive.query.repository.AbstractQueryRepository;
import com.gitee.dorive.sql.api.SqlRunner;
import com.gitee.dorive.sql.entity.SelectSegment;
import com.gitee.dorive.sql.entity.TableSegment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class CountQuerier {

    private AbstractQueryRepository<?, ?> repository;
    private SegmentBuilder segmentBuilder;
    private SqlRunner sqlRunner;

    public Map<String, Long> selectCountMap(Context context, CountQuery countQuery) {
        QueryContext queryContext = new QueryContext(context, ResultType.COUNT);
        QueryWrapper queryWrapper = new QueryWrapper(countQuery.getQuery());
        repository.resolveQuery(queryContext, queryWrapper);

        SelectSegment selectSegment = segmentBuilder.buildSegment(queryContext);
        TableSegment tableSegment = selectSegment.getTableSegment();
        List<Object> args = selectSegment.getArgs();
        String tableAlias = tableSegment.getTableAlias();

        EntityEle entityEle = repository.getEntityEle();
        String countByColumn = tableAlias + "." + entityEle.toAlias(countQuery.getCountBy());
        String groupByColumn = tableAlias + "." + entityEle.toAlias(countQuery.getGroupBy());

        List<String> columns = new ArrayList<>(2);
        columns.add(groupByColumn + " AS groupId");

        String format = "COUNT(%s) AS total";
        String countByColumnStr = String.format(format, countQuery.isDistinct() ? "DISTINCT " + countByColumn : countByColumn);
        columns.add(countByColumnStr);

        selectSegment.setSelectColumns(columns);
        selectSegment.setGroupBy("GROUP BY " + groupByColumn);

        List<Map<String, Object>> resultMaps = sqlRunner.selectList(selectSegment.toString(), args.toArray());
        Map<String, Long> countMap = new LinkedHashMap<>(resultMaps.size() * 4 / 3 + 1);
        resultMaps.forEach(resultMap -> countMap.put(resultMap.get("groupId").toString(), (Long) resultMap.get("total")));
        return countMap;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CountQuery {
        private Object query;
        private boolean distinct = true;
        private String countBy;
        private String groupBy;
    }

}
