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
import com.gitee.dorive.core.api.context.Matcher;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.InnerExample;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.query.api.QueryExecutor;
import com.gitee.dorive.query.entity.MergedRepository;
import com.gitee.dorive.query.entity.QueryContext;
import com.gitee.dorive.query.entity.QueryUnit;
import com.gitee.dorive.query.entity.enums.ResultType;
import com.gitee.dorive.query.impl.resolver.QueryTypeResolver;
import com.gitee.dorive.query.impl.resolver.QueryExampleResolver;
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
        resolve(queryContext);
        Matcher matcher = repository.getRootRepository();
        if (!matcher.matches(queryContext.getContext())) {
            return queryContext.newEmptyResult();
        }
        if (queryContext.isSimpleQuery()) {
            return executeRootQuery(queryContext);
        }
        return doExecuteQuery(queryContext);
    }

    protected void resolve(QueryContext queryContext) {
        Class<?> queryType = queryContext.getQueryType();
        QueryExampleResolver queryExampleResolver = getQueryExampleResolver(queryType);
        List<MergedRepository> mergedRepositories = getMergedRepositories(queryType);
        Assert.notNull(queryExampleResolver, "No query resolver found!");
        Assert.notEmpty(mergedRepositories, "The merged repositories cannot be empty!");
        queryContext.setQueryExampleResolver(queryExampleResolver);
        queryContext.setMergedRepositories(mergedRepositories);

        Map<String, Example> exampleMap = newExampleMap(queryContext);
        queryContext.setExampleMap(exampleMap);
        queryContext.setExample(exampleMap.get("/"));

        Map<String, QueryUnit> queryUnitMap = newQueryUnitMap(queryContext);
        queryContext.setQueryUnitMap(queryUnitMap);
        queryContext.setQueryUnit(queryUnitMap.get("/"));
    }

    protected QueryExampleResolver getQueryExampleResolver(Class<?> queryType) {
        QueryTypeResolver queryTypeResolver = repository.getQueryTypeResolver();
        Map<Class<?>, QueryExampleResolver> classQueryExampleResolverMap = queryTypeResolver.getClassQueryExampleResolverMap();
        return classQueryExampleResolverMap.get(queryType);
    }

    protected List<MergedRepository> getMergedRepositories(Class<?> queryType) {
        QueryTypeResolver queryTypeResolver = repository.getQueryTypeResolver();
        Map<Class<?>, List<MergedRepository>> classMergedRepositoriesMap = queryTypeResolver.getClassMergedRepositoriesMap();
        return classMergedRepositoriesMap.get(queryType);
    }

    protected Map<String, Example> newExampleMap(QueryContext queryContext) {
        Object query = queryContext.getQuery();
        QueryExampleResolver queryExampleResolver = queryContext.getQueryExampleResolver();
        return queryExampleResolver.resolve(query);
    }

    protected Map<String, QueryUnit> newQueryUnitMap(QueryContext queryContext) {
        List<MergedRepository> mergedRepositories = queryContext.getMergedRepositories();
        Map<String, Example> exampleMap = queryContext.getExampleMap();
        Map<String, QueryUnit> queryUnitMap = new LinkedHashMap<>();
        for (MergedRepository mergedRepository : mergedRepositories) {
            String absoluteAccessPath = mergedRepository.getAbsoluteAccessPath();
            String relativeAccessPath = mergedRepository.getRelativeAccessPath();
            Example example = exampleMap.computeIfAbsent(absoluteAccessPath, key -> new InnerExample());
            QueryUnit queryUnit = new QueryUnit(mergedRepository, example, false);
            queryUnitMap.put(relativeAccessPath, queryUnit);
        }
        return queryUnitMap;
    }

    @SuppressWarnings("unchecked")
    protected Result<Object> executeRootQuery(QueryContext queryContext) {
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

    protected abstract Result<Object> doExecuteQuery(QueryContext queryContext);

}
