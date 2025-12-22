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

package com.gitee.dorive.query2.v1.impl.segment;

import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.base.v1.core.api.Options;
import com.gitee.dorive.base.v1.core.entity.op.Result;
import com.gitee.dorive.base.v1.core.entity.qry.Example;
import com.gitee.dorive.base.v1.core.entity.qry.Page;
import com.gitee.dorive.base.v1.query.api.QueryExecutor;
import com.gitee.dorive.query2.v1.api.QueryResolver;
import com.gitee.dorive.query2.v1.api.SegmentExecutor;
import com.gitee.dorive.query2.v1.entity.executor.SegmentInfo;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SegmentQueryExecutor implements QueryExecutor {

    private final QueryResolver queryResolver;
    private final SegmentExecutor segmentExecutor;

    @Override
    public List<Object> selectByQuery(Options options, Object query) {
        Context context = (Context) options;
        SegmentInfo segmentInfo = (SegmentInfo) queryResolver.resolve(context, query);
        segmentExecutor.buildSelectColumns(segmentInfo);
        segmentExecutor.buildOrderByAndPage(segmentInfo);
        Result<Object> result = segmentExecutor.executeQuery(context, segmentInfo);
        return result.getRecords();
    }

    @Override
    public Page<Object> selectPageByQuery(Options options, Object query) {
        Context context = (Context) options;
        SegmentInfo segmentInfo = (SegmentInfo) queryResolver.resolve(context, query);
        segmentExecutor.buildSelectColumns(segmentInfo);
        // 查询总数
        long count = segmentExecutor.executeCount(segmentInfo);
        Example example = segmentInfo.getExample();
        if (example != null) {
            Page<Object> page = example.getPage();
            if (page != null) {
                page.setTotal(count);
            }
            if (count == 0L) {
                return page;
            }
        }
        segmentExecutor.buildOrderByAndPage(segmentInfo);
        Result<Object> result = segmentExecutor.executeQuery(context, segmentInfo);
        return result.getPage();
    }

    @Override
    public long selectCountByQuery(Options options, Object query) {
        SegmentInfo segmentInfo = (SegmentInfo) queryResolver.resolve((Context) options, query);
        segmentExecutor.buildSelectColumns(segmentInfo);
        return segmentExecutor.executeCount(segmentInfo);
    }

}
