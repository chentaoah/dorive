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

package com.gitee.dorive.query.v2.impl.segment;

import cn.hutool.core.lang.Assert;
import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.base.v1.core.entity.qry.Example;
import com.gitee.dorive.base.v1.core.entity.qry.InnerExample;
import com.gitee.dorive.base.v1.executor.api.Matcher;
import com.gitee.dorive.base.v1.factory.api.ExampleConverter;
import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import com.gitee.dorive.query.v2.api.QueryResolver;
import com.gitee.dorive.query.v2.api.SegmentResolver;
import com.gitee.dorive.query.v2.entity.core.QueryInfo;
import com.gitee.dorive.query.v2.entity.core.QueryRepositoryMapping;
import com.gitee.dorive.query.v2.entity.core.RepositoryInfo;
import com.gitee.dorive.query.v2.entity.segment.JoinInfo;
import com.gitee.dorive.query.v2.entity.segment.SegmentInfo;
import com.gitee.dorive.query.v2.impl.core.ExampleResolver;
import com.gitee.dorive.query.v2.impl.core.QueryInfoResolver;
import com.gitee.dorive.query.v2.impl.core.RepositoryInfoResolver;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class SegmentQueryResolver implements QueryResolver {

    private final RepositoryInfoResolver repositoryInfoResolver;
    private final QueryInfoResolver queryInfoResolver;
    private final SegmentResolver segmentResolver;

    @Override
    public Object resolve(Context context, Object query) {
        Matcher matcher = context.getOption(Matcher.class);

        QueryInfo queryInfo = queryInfoResolver.findQueryInfo(query.getClass());
        Assert.notNull(queryInfo, "No query info found!");
        List<QueryRepositoryMapping> reversedQueryRepositoryMappings = queryInfo.getReversedQueryRepositoryMappings();
        ExampleResolver exampleResolver = queryInfo.getExampleResolver();

        // 仓储 => 别名
        Map<RepositoryContext, String> repositoryAliasMap = new LinkedHashMap<>(8);
        // 连接信息
        List<JoinInfo> joinInfos = new ArrayList<>();
        // 仓储 => 条件
        Map<RepositoryContext, Example> repositoryExampleMap = new LinkedHashMap<>(8);

        // 仓储信息 => (属性 => 条件)
        Map<RepositoryInfo, Map<String, Example>> nodeExampleMapMap = new LinkedHashMap<>(8);
        // 片段信息
        SegmentInfo segmentInfo = new SegmentInfo();

        for (QueryRepositoryMapping queryRepositoryMapping : reversedQueryRepositoryMappings) {
            RepositoryInfo repositoryInfo = queryRepositoryMapping.getRepositoryInfo();
            RepositoryInfo parent = repositoryInfo.getParent();
            String lastAccessPath = repositoryInfo.getLastAccessPath();
            RepositoryItem lastRepositoryItem = repositoryInfo.getLastRepositoryItem();
            RepositoryContext repositoryContext = repositoryInfo.getRepositoryContext();

            // 别名
            String alias = "t" + repositoryInfo.getSequence();
            repositoryAliasMap.put(repositoryContext, alias);

            // 选取
            RepositoryItem repositoryItem = lastRepositoryItem != null ? lastRepositoryItem : repositoryContext.getRootRepository();
            if (matcher != null && matcher.matches(repositoryItem)) {
                segmentInfo.setSelectedRepository(repositoryContext);
                segmentInfo.setSelectedRepositoryAlias(alias);
            }

            // 如果被激活，则解析连接条件
            Map<String, Example> exampleMap = nodeExampleMapMap.get(repositoryInfo);
            if (exampleMap != null) {
                // 20260406 ct 修复因简化代码，而导致的逻辑错误，这里的JoinInfoResolver不能是同一个
                JoinInfoResolver joinInfoResolver = repositoryContext.getProperty(JoinInfoResolver.class);
                List<JoinInfo> joins = joinInfoResolver.resolve(context, exampleMap.keySet());
                joinInfos.addAll(joins);
                // 额外分配别名和筛选条件
                for (JoinInfo join : joins) {
                    RepositoryContext joiner = join.getJoiner();
                    if (!repositoryAliasMap.containsKey(joiner)) {
                        RepositoryInfo joinRepositoryInfo = repositoryInfoResolver.findRepositoryInfo(joiner);
                        repositoryAliasMap.put(joiner, "t" + joinRepositoryInfo.getSequence());
                        repositoryExampleMap.put(joiner, new InnerExample());
                    }
                }
            }

            // 筛选条件
            Example example = new InnerExample();
            queryRepositoryMapping.appendCriteria(query, example);
            repositoryExampleMap.put(repositoryContext, example);

            if (parent != null) {
                if (exampleMap != null || !example.isEmpty()) {
                    Map<String, Example> parentExampleMap = nodeExampleMapMap.computeIfAbsent(parent, k -> new LinkedHashMap<>(8));
                    parentExampleMap.put(lastAccessPath, example);
                }
            } else {
                segmentInfo.setRepository(repositoryContext);
                segmentInfo.setExample(example);
            }
        }

        Example example = segmentInfo.getExample();
        if (example != null) {
            example.setOrderBy(exampleResolver.newOrderBy(query));
            example.setPage(exampleResolver.newPage(query));
        } else {
            return null;
        }

        // 转化筛选条件
        repositoryExampleMap.forEach(((repository, eachExample) -> repository.getProperty(ExampleConverter.class).convert(context, eachExample)));
        Collections.reverse(joinInfos);
        Object segment = segmentResolver.resolve(repositoryAliasMap, joinInfos, repositoryExampleMap, segmentInfo.getRepository(), example);
        segmentInfo.setSegment(segment);

        return segmentInfo;
    }
}
