/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.query.v2.impl.stepwise;

import cn.hutool.core.lang.Assert;
import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.base.v1.executor.entity.qry.Example;
import com.gitee.dorive.base.v1.executor.entity.qry.InnerExample;
import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import com.gitee.dorive.query.v2.impl.core.ExampleResolver;
import com.gitee.dorive.query.v2.impl.core.QueryInfoResolver;
import com.gitee.dorive.query.v2.api.QueryResolver;
import com.gitee.dorive.query.v2.entity.core.QueryInfo;
import com.gitee.dorive.query.v2.entity.core.QueryRepositoryMapping;
import com.gitee.dorive.query.v2.entity.core.RepositoryInfo;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class StepwiseQueryResolver implements QueryResolver {

    private final QueryInfoResolver queryInfoResolver;

    @Override
    public Object resolve(Context context, Object query) {
        QueryInfo queryInfo = queryInfoResolver.findQueryInfo(query.getClass());
        Assert.notNull(queryInfo, "No query info found!");
        List<QueryRepositoryMapping> reversedQueryRepositoryMappings = queryInfo.getReversedQueryRepositoryMappings();
        ExampleResolver exampleResolver = queryInfo.getExampleResolver();

        Map<RepositoryInfo, Map<String, Example>> nodeExampleMapMap = new LinkedHashMap<>(8);
        Example rootExample = null;

        for (QueryRepositoryMapping queryRepositoryMapping : reversedQueryRepositoryMappings) {
            RepositoryInfo repositoryInfo = queryRepositoryMapping.getRepositoryInfo();
            RepositoryInfo parent = repositoryInfo.getParent();
            String lastAccessPath = repositoryInfo.getLastAccessPath();
            RepositoryContext repositoryContext = repositoryInfo.getRepositoryContext();

            Map<String, Example> exampleMap = nodeExampleMapMap.get(repositoryInfo);
            Example example;
            if (exampleMap != null) {
                StepwiseQuerier stepwiseQuerier = repositoryContext.getProperty(StepwiseQuerier.class);
                example = stepwiseQuerier.executeQuery(context, exampleMap);
            } else {
                example = new InnerExample();
            }
            queryRepositoryMapping.appendCriteria(query, example);

            if (parent != null) {
                if (example != null && !example.isEmpty()) {
                    Map<String, Example> parentExampleMap = nodeExampleMapMap.computeIfAbsent(parent, k -> new LinkedHashMap<>(8));
                    parentExampleMap.put(lastAccessPath, example);
                }
            } else {
                rootExample = example;
            }
        }

        if (rootExample != null) {
            rootExample.setOrderBy(exampleResolver.newOrderBy(query));
            rootExample.setPage(exampleResolver.newPage(query));
        }

        return rootExample;
    }
}
