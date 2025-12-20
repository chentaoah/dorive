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

package com.gitee.dorive.query2.v1.impl.core;

import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.base.v1.core.entity.qry.Example;
import com.gitee.dorive.base.v1.core.entity.qry.InnerExample;
import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import com.gitee.dorive.query2.v1.api.QueryResolver;
import com.gitee.dorive.query2.v1.entity.QueryConfig;
import com.gitee.dorive.query2.v1.entity.QueryNode;
import com.gitee.dorive.query2.v1.entity.RepositoryNode;
import com.gitee.dorive.query2.v1.impl.resolver.ExampleResolver;
import com.gitee.dorive.query2.v1.impl.resolver.QueryConfigResolver;
import com.gitee.dorive.query2.v1.impl.querier.ReverseQuerier;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class ReverseQueryResolver implements QueryResolver {

    private final QueryConfigResolver queryConfigResolver;

    @Override
    public Example newExample(Context context, Object query) {
        Class<?> queryClass = query.getClass();

        Map<Class<?>, QueryConfig> classQueryConfigMap = queryConfigResolver.getClassQueryConfigMap();
        QueryConfig queryConfig = classQueryConfigMap.get(queryClass);
        List<QueryNode> reversedQueryNodes = queryConfig.getReversedQueryNodes();
        ExampleResolver exampleResolver = queryConfig.getExampleResolver();

        Map<RepositoryNode, Map<String, Example>> nodeExampleMapMap = new LinkedHashMap<>(8);
        Example rootExample = null;

        for (QueryNode queryNode : reversedQueryNodes) {
            RepositoryNode repositoryNode = queryNode.getRepositoryNode();
            RepositoryNode parent = repositoryNode.getParent();
            String lastAccessPath = repositoryNode.getLastAccessPath();
            RepositoryContext repository = repositoryNode.getRepository();

            Example example;
            Map<String, Example> exampleMap = nodeExampleMapMap.get(repositoryNode);
            if (exampleMap != null) {
                ReverseQuerier reverseQuerier = repository.getProperty(ReverseQuerier.class);
                example = reverseQuerier.executeQuery(context, exampleMap);
            } else {
                example = new InnerExample();
            }
            queryNode.appendCriteria(query, example);

            if (parent != null) {
                Map<String, Example> parentExampleMap = nodeExampleMapMap.computeIfAbsent(parent, k -> new LinkedHashMap<>(8));
                parentExampleMap.put(lastAccessPath, example);
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
