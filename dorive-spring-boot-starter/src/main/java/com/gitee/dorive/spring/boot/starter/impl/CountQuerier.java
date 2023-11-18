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

package com.gitee.dorive.spring.boot.starter.impl;

import com.baomidou.mybatisplus.extension.toolkit.SqlRunner;
import com.gitee.dorive.api.entity.element.EntityEle;
import com.gitee.dorive.query.entity.QueryCtx;
import com.gitee.dorive.query.repository.AbstractQueryRepository;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.entity.context.BoundedContext;
import com.gitee.dorive.spring.boot.starter.entity.segment.SegmentResult;
import com.gitee.dorive.spring.boot.starter.entity.segment.SelectSegment;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class CountQuerier {

    private final AbstractQueryRepository<?, ?> repository;
    private final SegmentBuilder segmentBuilder;

    public CountQuerier(AbstractQueryRepository<?, ?> repository) {
        this.repository = repository;
        this.segmentBuilder = new SegmentBuilder();
    }

    public Map<String, Long> selectCount(Context context, String groupField, boolean distinct, String countField, Object query) {
        QueryCtx queryCtx = repository.newQuery(query);

        SegmentResult segmentResult = segmentBuilder.buildSegment(context, queryCtx);
        SelectSegment selectSegment = segmentResult.getSelectSegment();
        List<Object> args = segmentResult.getArgs();

        EntityEle entityEle = repository.getEntityEle();
        String groupByColumn = entityEle.toAlias(groupField);
        String countColumn = entityEle.toAlias(countField);

        String tableAlias = selectSegment.getTableAlias();
        groupByColumn = tableAlias + "." + groupByColumn;
        countColumn = tableAlias + "." + countColumn;

        List<String> columns = new ArrayList<>(2);
        columns.add(groupByColumn + " AS recordId");
        if (distinct) {
            columns.add("count(DISTINCT " + countColumn + ") AS totalCount");
        } else {
            columns.add("count(" + countColumn + ") AS totalCount");
        }
        selectSegment.setColumns(columns);
        selectSegment.setGroupBy("GROUP BY " + groupByColumn);

        List<Map<String, Object>> resultMaps = SqlRunner.db().selectList(selectSegment.toString(), args.toArray());
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
