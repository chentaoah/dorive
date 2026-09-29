/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.query.v2.impl.segment;

import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.base.v1.executor.api.Options;
import com.gitee.dorive.base.v1.executor.entity.op.Result;
import com.gitee.dorive.base.v1.executor.entity.qry.Example;
import com.gitee.dorive.base.v1.executor.entity.qry.Page;
import com.gitee.dorive.base.v1.query.api.QueryExecutor;
import com.gitee.dorive.query.v2.api.QueryResolver;
import com.gitee.dorive.query.v2.api.SegmentExecutor;
import com.gitee.dorive.query.v2.entity.segment.SegmentInfo;
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
                if (count == 0L) {
                    return page;
                }
            }
        }
        if (count == 0L) {
            return new Page<>();
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
