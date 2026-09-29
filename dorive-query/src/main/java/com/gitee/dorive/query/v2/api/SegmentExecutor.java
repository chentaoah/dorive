/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.query.v2.api;

import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.base.v1.executor.entity.op.Result;
import com.gitee.dorive.query.v2.entity.segment.SegmentInfo;

public interface SegmentExecutor {

    void buildSelectColumns(SegmentInfo segmentInfo);

    long executeCount(SegmentInfo segmentInfo);

    void buildOrderByAndPage(SegmentInfo segmentInfo);

    Result<Object> executeQuery(Context context, SegmentInfo segmentInfo);

}
