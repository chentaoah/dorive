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

import com.gitee.dorive.api.entity.ele.EntityElement;
import com.gitee.dorive.core.api.context.Selector;
import com.gitee.dorive.core.repository.CommonRepository;
import com.gitee.dorive.query.entity.MergedRepository;
import com.gitee.dorive.query.entity.QueryContext;
import com.gitee.dorive.query.entity.QueryUnit;
import com.gitee.dorive.sql.entity.common.SegmentInfo;
import com.gitee.dorive.sql.entity.common.SegmentUnit;
import com.gitee.dorive.sql.entity.segment.SelectSegment;
import com.gitee.dorive.sql.entity.segment.TableJoinSegment;
import com.gitee.dorive.sql.entity.segment.TableSegment;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SelectSegmentBuilder {

    private QueryContext queryContext;
    private List<SegmentInfo> matchedSegmentInfos;

    public SelectSegmentBuilder(QueryContext queryContext) {
        this.queryContext = queryContext;
    }

    public SelectSegment build(Selector selector) {
        if (selector != null) {
            this.matchedSegmentInfos = new ArrayList<>(8);
        }
        SelectSegment selectSegment = new SelectSegment();
        for (QueryUnit queryUnit : queryContext.getQueryUnitMap().values()) {
            SegmentUnit segmentUnit = (SegmentUnit) queryUnit;
            MergedRepository mergedRepository = queryUnit.getMergedRepository();
            TableSegment tableSegment = segmentUnit.getTableSegment();

            String absoluteAccessPath = mergedRepository.getAbsoluteAccessPath();
            CommonRepository definedRepository = mergedRepository.getDefinedRepository();
            CommonRepository executedRepository = mergedRepository.getExecutedRepository();
            EntityElement entityElement = executedRepository.getEntityElement();

            SegmentInfo segmentInfo = new SegmentInfo(tableSegment.getTableAlias(), entityElement);
            boolean isMatch = selector != null && definedRepository.matches(selector);
            if (isMatch) {
                matchedSegmentInfos.add(segmentInfo);
            }
            tableSegment.setJoin(tableSegment.isJoin() || isMatch);

            if ("/".equals(absoluteAccessPath)) {
                selectSegment.setTableSegment(tableSegment);
            } else {
                TableJoinSegment tableJoinSegment = (TableJoinSegment) tableSegment;
                selectSegment.getTableJoinSegments().add(tableJoinSegment);
            }
        }
        selectSegment.filterTableSegments();
        selectSegment.setArgs(queryContext.getArgs());
        return selectSegment;
    }

}
