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

package com.gitee.dorive.sql.impl.segment;

import com.gitee.dorive.core.api.context.Selector;
import com.gitee.dorive.core.repository.CommonRepository;
import com.gitee.dorive.query.entity.MergedRepository;
import com.gitee.dorive.query.entity.QueryContext;
import com.gitee.dorive.query.entity.QueryUnit;
import com.gitee.dorive.sql.entity.common.SegmentUnit;
import com.gitee.dorive.sql.entity.segment.SelectSegment;
import com.gitee.dorive.sql.entity.segment.TableJoinSegment;
import com.gitee.dorive.sql.entity.segment.TableSegment;
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

    public List<SegmentUnit> select(Selector selector) {
        List<SegmentUnit> segmentUnits = new ArrayList<>(4);
        Map<String, QueryUnit> queryUnitMap = queryContext.getQueryUnitMap();
        for (QueryUnit queryUnit : queryUnitMap.values()) {
            SegmentUnit segmentUnit = (SegmentUnit) queryUnit;
            MergedRepository mergedRepository = queryUnit.getMergedRepository();
            CommonRepository definedRepository = mergedRepository.getDefinedRepository();
            boolean isMatch = definedRepository.matches(selector);
            if (isMatch) {
                TableSegment tableSegment = segmentUnit.getTableSegment();
                tableSegment.setJoin(true);
                segmentUnits.add(segmentUnit);
            }
        }
        return segmentUnits;
    }

    public SelectSegment build() {
        SelectSegment selectSegment = new SelectSegment();

        SegmentUnit segmentUnit = (SegmentUnit) queryContext.getQueryUnit();
        TableSegment tableSegment = segmentUnit.getTableSegment();
        if (tableSegment.isJoin()) {
            selectSegment.setTableSegment(tableSegment);
        }

        Map<String, QueryUnit> queryUnitMap = queryContext.getQueryUnitMap();
        queryUnitMap.forEach((absoluteAccessPath, queryUnit) -> {
            if (!"/".equals(absoluteAccessPath)) {
                SegmentUnit subSegmentUnit = (SegmentUnit) queryUnit;
                TableJoinSegment tableJoinSegment = (TableJoinSegment) subSegmentUnit.getTableSegment();
                if (tableJoinSegment.isJoin()) {
                    selectSegment.getTableJoinSegments().add(tableJoinSegment);
                }
            }
        });

        selectSegment.setArgs(queryContext.getArgs());

        return selectSegment;
    }

}
