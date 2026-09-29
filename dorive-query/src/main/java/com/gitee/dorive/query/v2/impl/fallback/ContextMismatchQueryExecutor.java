/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.query.v2.impl.fallback;

import cn.hutool.core.lang.Assert;
import com.gitee.dorive.base.v1.executor.api.Options;
import com.gitee.dorive.base.v1.executor.entity.qry.Page;
import com.gitee.dorive.base.v1.query.api.QueryExecutor;
import com.gitee.dorive.query.v2.impl.core.ExampleResolver;
import com.gitee.dorive.query.v2.impl.core.QueryInfoResolver;
import com.gitee.dorive.query.v2.entity.core.QueryInfo;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
@AllArgsConstructor
public class ContextMismatchQueryExecutor implements QueryExecutor {

    private final QueryInfoResolver queryInfoResolver;

    @Override
    public List<Object> selectByQuery(Options options, Object query) {
        return Collections.emptyList();
    }

    @Override
    public Page<Object> selectPageByQuery(Options options, Object query) {
        QueryInfo queryInfo = queryInfoResolver.findQueryInfo(query.getClass());
        Assert.notNull(queryInfo, "No query info found!");
        ExampleResolver exampleResolver = queryInfo.getExampleResolver();
        return exampleResolver.newPage(query);
    }

    @Override
    public long selectCountByQuery(Options options, Object query) {
        return 0L;
    }

}
