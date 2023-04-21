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
import cn.hutool.db.sql.SqlBuilder;
import com.baomidou.mybatisplus.extension.toolkit.SqlRunner;
import com.gitee.dorive.coating.api.ExampleBuilder;
import com.gitee.dorive.coating.repository.AbstractCoatingRepository;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.OrderBy;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.spring.boot.starter.entity.SegmentResult;
import com.gitee.dorive.spring.boot.starter.entity.SelectSegment;
import lombok.Data;

import java.util.Collections;
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
    public Example buildExample(Context context, Object coating) {
        SegmentResult segmentResult = segmentBuilder.buildSegment(coating);
        char letter = segmentResult.getLetter();
        SelectSegment selectSegment = segmentResult.getSelectSegment();
        List<Object> args = segmentResult.getArgs();
        OrderBy orderBy = segmentResult.getOrderBy();
        Page<Object> page = segmentResult.getPage();

        Example example = new Example();
        example.setOrderBy(orderBy);
        example.setPage(page);

        if (selectSegment == null) {
            throw new RuntimeException("Unable to build SQL statement!");
        }
        if (selectSegment.getArgSegments().isEmpty()) {
            return example;
        }
        if (!selectSegment.isDirtyQuery()) {
            return example;
        }
        
        selectSegment.setDistinct(true);
        selectSegment.setColumns(Collections.singletonList(selectSegment.getTableAlias() + ".id"));
        SqlBuilder builder = selectSegment.createBuilder();

        if (page != null) {
            long count = SqlRunner.db().selectCount("SELECT COUNT(1) FROM (" + builder + ") " + letter, args.toArray());
            page.setTotal(count);
            example.setCountQueried(true);
            if (count == 0) {
                example.setEmptyQuery(true);
                return example;
            }
        }

        if (orderBy != null) {
            builder.append(" ").append(orderBy.toString());
        }
        if (page != null) {
            builder.append(" ").append(page.toString());
        }

        List<Map<String, Object>> resultMaps = SqlRunner.db().selectList(builder.toString(), args.toArray());
        List<Object> primaryKeys = CollUtil.map(resultMaps, map -> map.get("id"), true);
        if (!primaryKeys.isEmpty()) {
            example.eq("id", primaryKeys);
        } else {
            example.setEmptyQuery(true);
        }

        return example;
    }

}
