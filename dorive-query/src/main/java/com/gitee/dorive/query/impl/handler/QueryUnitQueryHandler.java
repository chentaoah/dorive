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

package com.gitee.dorive.query.impl.handler;

import cn.hutool.core.lang.Assert;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.InnerExample;
import com.gitee.dorive.query.api.QueryHandler;
import com.gitee.dorive.query.entity.MergedRepository;
import com.gitee.dorive.query.entity.QueryContext;
import com.gitee.dorive.query.entity.QueryUnit;
import com.gitee.dorive.query.impl.resolver.QueryTypeResolver;
import com.gitee.dorive.query.repository.AbstractQueryRepository;
import lombok.AllArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class QueryUnitQueryHandler implements QueryHandler {

    protected final AbstractQueryRepository<?, ?> repository;
    protected final QueryHandler queryHandler;

    @Override
    public void handle(QueryContext queryContext, Object query) {
        QueryTypeResolver queryTypeResolver = repository.getQueryTypeResolver();
        Map<Class<?>, List<MergedRepository>> classMergedRepositoriesMap = getClassMergedRepositoriesMap(queryTypeResolver);

        Class<?> queryType = queryContext.getQueryType();
        List<MergedRepository> mergedRepositories = classMergedRepositoriesMap.get(queryType);

        Assert.notEmpty(mergedRepositories, "The merged repositories cannot be empty!");
        queryContext.setMergedRepositories(mergedRepositories);

        Map<String, QueryUnit> queryUnitMap = newQueryUnitMap(queryContext);
        queryContext.setQueryUnit(queryUnitMap.get("/"));

        if (queryHandler != null) {
            queryHandler.handle(queryContext, query);
        }
    }

    protected Map<Class<?>, List<MergedRepository>> getClassMergedRepositoriesMap(QueryTypeResolver queryTypeResolver) {
        return queryTypeResolver.getClassMergedRepositoriesMap();
    }

    private Map<String, QueryUnit> newQueryUnitMap(QueryContext queryContext) {
        List<MergedRepository> mergedRepositories = queryContext.getMergedRepositories();
        Map<String, Example> exampleMap = queryContext.getExampleMap();
        Map<String, QueryUnit> queryUnitMap = new LinkedHashMap<>();
        // 后续可能用到
        queryContext.setQueryUnitMap(queryUnitMap);
        for (MergedRepository mergedRepository : mergedRepositories) {
            String absoluteAccessPath = mergedRepository.getAbsoluteAccessPath();
            Example example = exampleMap.computeIfAbsent(absoluteAccessPath, key -> new InnerExample());
            QueryUnit queryUnit = newQueryUnit(queryContext, mergedRepository, example);
            queryUnitMap.put(absoluteAccessPath, queryUnit);
        }
        return queryUnitMap;
    }

    protected QueryUnit newQueryUnit(QueryContext queryContext, MergedRepository mergedRepository, Example example) {
        return new QueryUnit(mergedRepository, example, false);
    }

}
