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
import com.gitee.dorive.core.entity.context.BoundedContext;
import com.gitee.dorive.query.entity.BuildQuery;
import com.gitee.dorive.query.repository.AbstractQueryRepository;
import com.gitee.dorive.sql.api.SqlHelper;
import com.gitee.dorive.sql.entity.SelectSegment;
import com.gitee.dorive.sql.entity.TableSegment;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class CountQuerier {

    private AbstractQueryRepository<?, ?> repository;
    private SegmentBuilder segmentBuilder;
    private SqlHelper sqlHelper;

    public Map<String, Long> selectCount(Context context, String groupField, boolean distinct, String countField, Object query) {
        BuildQuery buildQuery = repository.doNewQuery(context, query, false);

        SelectSegment selectSegment = segmentBuilder.buildSegment(context, buildQuery);
        TableSegment tableSegment = selectSegment.getTableSegment();
        List<Object> args = selectSegment.getArgs();

        EntityEle entityEle = repository.getEntityEle();
        String groupByColumn = entityEle.toAlias(groupField);
        String countColumn = entityEle.toAlias(countField);

        String tableAlias = tableSegment.getTableAlias();
        groupByColumn = tableAlias + "." + groupByColumn;
        countColumn = tableAlias + "." + countColumn;

        List<String> columns = new ArrayList<>(2);
        columns.add(groupByColumn + " AS recordId");
        if (distinct) {
            columns.add("count(DISTINCT " + countColumn + ") AS totalCount");
        } else {
            columns.add("count(" + countColumn + ") AS totalCount");
        }
        selectSegment.setSelectColumns(columns);
        selectSegment.setGroupBy("GROUP BY " + groupByColumn);

        List<Map<String, Object>> resultMaps = sqlHelper.selectList(selectSegment.toString(), args.toArray());
        Map<String, Long> countMap = new LinkedHashMap<>(resultMaps.size() * 4 / 3 + 1);
        resultMaps.forEach(resultMap -> countMap.put(resultMap.get("recordId").toString(), (Long) resultMap.get("totalCount")));
        return countMap;
    }

    public Map<String, Long> selectCount(Context context, String groupField, String countField, Object query) {
        return selectCount(context, groupField, true, countField, query);
    }

    public Map<String, Long> selectCount(String groupField, String countField, Object query) {
        return selectCount(new BoundedContext(), groupField, true, countField, query);
    }

}
