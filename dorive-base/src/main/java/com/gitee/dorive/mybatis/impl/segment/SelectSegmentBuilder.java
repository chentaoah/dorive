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

package com.gitee.dorive.mybatis.impl.segment;

import com.gitee.dorive.core.api.context.Selector;
import com.gitee.dorive.core.impl.repository.ProxyRepository;
import com.gitee.dorive.mybatis.entity.segment.ArgSegment;
import com.gitee.dorive.mybatis.entity.segment.SelectSegment;
import com.gitee.dorive.mybatis.entity.segment.TableJoinSegment;
import com.gitee.dorive.mybatis.entity.segment.TableSegment;
import com.gitee.dorive.query.entity.MergedRepository;
import com.gitee.dorive.query.entity.QueryContext;
import com.gitee.dorive.query.entity.QueryUnit;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class SelectSegmentBuilder {

    private QueryContext queryContext;

    public SelectSegmentBuilder(QueryContext queryContext) {
        this.queryContext = queryContext;
    }

    public List<QueryUnit> select(Selector selector) {
        List<QueryUnit> queryUnits = new ArrayList<>(4);
        Map<String, QueryUnit> queryUnitMap = queryContext.getQueryUnitMap();
        for (QueryUnit queryUnit : queryUnitMap.values()) {
            MergedRepository mergedRepository = queryUnit.getMergedRepository();
            ProxyRepository definedRepository = mergedRepository.getDefinedRepository();
            boolean isMatch = definedRepository.matches(selector);
            if (isMatch) {
                TableSegment tableSegment = (TableSegment) queryUnit.getAttachment();
                tableSegment.setJoin(true);
                queryUnits.add(queryUnit);
            }
        }
        return queryUnits;
    }

    public SelectSegment build() {
        SelectSegment selectSegment = new SelectSegment();
        List<TableJoinSegment> tableJoinSegments = selectSegment.getTableJoinSegments();
        List<ArgSegment> argSegments = selectSegment.getArgSegments();

        Map<String, QueryUnit> queryUnitMap = queryContext.getQueryUnitMap();
        for (QueryUnit queryUnit : queryUnitMap.values()) {
            TableSegment tableSegment = (TableSegment) queryUnit.getAttachment();
            if (queryUnit.isRoot()) {
                selectSegment.setTableSegment(tableSegment);

            } else if (tableSegment.isJoin()) {
                tableJoinSegments.add((TableJoinSegment) tableSegment);
            }
            if (tableSegment.isJoin()) {
                argSegments.addAll(tableSegment.getArgSegments());
            }
        }
        selectSegment.setArgs(queryContext.getArgs());

        return selectSegment;
    }

}
