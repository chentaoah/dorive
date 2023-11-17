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

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.toolkit.SqlRunner;
import com.gitee.dorive.query.api.ExampleBuilder;
import com.gitee.dorive.query.entity.BuildExample;
import com.gitee.dorive.query.repository.AbstractCoatingRepository;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.entity.executor.OrderBy;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.spring.boot.starter.entity.segment.SegmentResult;
import com.gitee.dorive.spring.boot.starter.entity.segment.SelectSegment;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class SQLExampleBuilder implements ExampleBuilder {

    private final AbstractCoatingRepository<?, ?> repository;
    private final SegmentBuilder segmentBuilder;

    public SQLExampleBuilder(AbstractCoatingRepository<?, ?> repository) {
        this.repository = repository;
        this.segmentBuilder = new SegmentBuilder(repository);
    }

    @Override
    public BuildExample buildExample(Context context, Object coating) {
        SegmentResult segmentResult = segmentBuilder.buildSegment(context, coating);
        char letter = segmentResult.getLetter();
        SelectSegment selectSegment = segmentResult.getSelectSegment();
        List<Object> args = segmentResult.getArgs();
        OrderBy orderBy = segmentResult.getOrderBy();
        Page<Object> page = segmentResult.getPage();

        BuildExample buildExample = new BuildExample();
        buildExample.setOrderBy(orderBy);
        buildExample.setPage(page);

        if (selectSegment == null) {
            throw new RuntimeException("Unable to build SQL statement!");
        }
        if (selectSegment.getArgSegments().isEmpty()) {
            return buildExample;
        }
        if (!selectSegment.isDirtyQuery()) {
            return buildExample;
        }

        selectSegment.setDistinct(true);

        List<String> selectColumns = new ArrayList<>(2);
        String tableAlias = selectSegment.getTableAlias();
        selectColumns.add(tableAlias + ".id");
        selectSegment.setColumns(selectColumns);

        String fromWhereSql = selectSegment.fromWhereSql();

        if (page != null) {
            String countSql = selectSegment.selectSql() + fromWhereSql;
            long count = SqlRunner.db().selectCount("SELECT COUNT(*) AS total FROM (" + countSql + ") " + letter, args.toArray());
            page.setTotal(count);
            buildExample.setCountQueried(true);
            if (count == 0) {
                buildExample.setAbandoned(true);
                return buildExample;
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
        List<Map<String, Object>> resultMaps = SqlRunner.db().selectList(selectSql, args.toArray());
        List<Object> primaryKeys = CollUtil.map(resultMaps, map -> map.get("id"), true);
        if (!primaryKeys.isEmpty()) {
            buildExample.eq("id", primaryKeys);
        } else {
            buildExample.setAbandoned(true);
        }

        return buildExample;
    }

}
