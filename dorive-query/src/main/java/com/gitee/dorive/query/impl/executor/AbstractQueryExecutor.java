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

package com.gitee.dorive.query.impl.executor;

import cn.hutool.core.lang.Assert;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.InnerExample;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.query.api.QueryExecutor;
import com.gitee.dorive.query.entity.MergedRepository;
import com.gitee.dorive.query.entity.QueryContext;
import com.gitee.dorive.query.entity.QueryUnit;
import com.gitee.dorive.query.entity.enums.ResultType;
import com.gitee.dorive.query.impl.resolver.QueryRepositoryResolver;
import com.gitee.dorive.query.impl.resolver.QueryResolver;
import com.gitee.dorive.query.repository.AbstractQueryRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractQueryExecutor implements QueryExecutor {

    protected final AbstractQueryRepository<?, ?> repository;

    public AbstractQueryExecutor(AbstractQueryRepository<?, ?> repository) {
        this.repository = repository;
    }

    @Override
    public Result<Object> executeQuery(QueryContext queryContext) {
        Object query = queryContext.getQuery();

        Map<String, Example> exampleMap = resolveQuery(query);
        queryContext.setExampleMap(exampleMap);
        queryContext.setExample(exampleMap.get("/"));

        return doExecuteQuery(queryContext);
    }

    protected Map<String, Example> resolveQuery(Object query) {
        QueryRepositoryResolver queryRepositoryResolver = repository.getQueryRepositoryResolver();
        Map<Class<?>, QueryResolver> classQueryResolverMap = queryRepositoryResolver.getClassQueryResolverMap();
        QueryResolver queryResolver = classQueryResolverMap.get(query.getClass());
        Assert.notNull(queryResolver, "No query resolver found!");
        return queryResolver.resolve(query);
    }

    private Map<String, QueryUnit> newQueryUnitMap(QueryContext queryContext) {
        QueryRepositoryResolver queryRepositoryResolver = repository.getQueryRepositoryResolver();
        Map<Class<?>, List<MergedRepository>> classMergedRepositoriesMap = queryRepositoryResolver.getClassMergedRepositoriesMap();

        Map<String, Example> exampleMap = queryContext.getExampleMap();
        Map<String, QueryUnit> exampleWrapperMap = new LinkedHashMap<>();
        for (MergedRepository mergedRepository : queryResolver.getReversedMergedRepositories()) {
            String absoluteAccessPath = mergedRepository.getAbsoluteAccessPath();
            String relativeAccessPath = mergedRepository.getRelativeAccessPath();
            Example example = exampleMap.computeIfAbsent(absoluteAccessPath, key -> new InnerExample());
            QueryUnit queryUnit = new QueryUnit(mergedRepository, example, false);
            exampleWrapperMap.put(relativeAccessPath, queryUnit);
        }
        return exampleWrapperMap;
    }

    @SuppressWarnings("unchecked")
    protected Result<Object> doExecuteQuery(QueryContext queryContext) {
        Context context = queryContext.getContext();
        ResultType resultType = queryContext.getResultType();
        Example example = queryContext.getExample();
        if (resultType == ResultType.COUNT_AND_DATA) {
            Page<Object> page = (Page<Object>) repository.selectPageByExample(context, example);
            return new Result<>(page);

        } else if (resultType == ResultType.DATA) {
            List<Object> entities = (List<Object>) repository.selectByExample(context, example);
            return new Result<>(entities);

        } else if (resultType == ResultType.COUNT) {
            long count = repository.selectCountByExample(context, example);
            return new Result<>(count);
        }
        return queryContext.newEmptyResult();
    }

}
