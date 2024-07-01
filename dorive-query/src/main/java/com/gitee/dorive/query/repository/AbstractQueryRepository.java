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

package com.gitee.dorive.query.repository;

import cn.hutool.core.lang.Assert;
import com.gitee.dorive.api.annotation.Repository;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.context.Matcher;
import com.gitee.dorive.core.api.context.Options;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.event.repository.AbstractEventRepository;
import com.gitee.dorive.query.api.QueryExecutor;
import com.gitee.dorive.query.api.QueryRepository;
import com.gitee.dorive.query.entity.QueryContext;
import com.gitee.dorive.query.entity.QueryWrapper;
import com.gitee.dorive.query.entity.def.QueryScanDef;
import com.gitee.dorive.query.entity.enums.ResultType;
import com.gitee.dorive.query.impl.executor.SimpleQueryExecutor;
import com.gitee.dorive.query.impl.executor.StepwiseQueryExecutor;
import com.gitee.dorive.query.impl.resolver.MergedRepositoryResolver;
import com.gitee.dorive.query.impl.resolver.QueryResolver;
import com.gitee.dorive.query.impl.resolver.QueryTypeResolver;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public abstract class AbstractQueryRepository<E, PK> extends AbstractEventRepository<E, PK> implements QueryRepository<E, PK>, QueryExecutor {

    private QueryScanDef queryScanDef;
    private MergedRepositoryResolver mergedRepositoryResolver;
    private QueryTypeResolver queryTypeResolver;
    private QueryExecutor simpleQueryExecutor;
    private QueryExecutor stepwiseQueryExecutor;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Repository repository = AnnotatedElementUtils.getMergedAnnotation(this.getClass(), Repository.class);
        this.queryScanDef = QueryScanDef.fromElement(this.getClass());
        if (repository != null) {
            this.mergedRepositoryResolver = new MergedRepositoryResolver(this);
            mergedRepositoryResolver.resolve();
        }
        if (repository != null && queryScanDef != null) {
            this.queryTypeResolver = new QueryTypeResolver(this);
            this.simpleQueryExecutor = new SimpleQueryExecutor(this);
            this.stepwiseQueryExecutor = new StepwiseQueryExecutor(this);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<E> selectByQuery(Options options, Object query) {
        QueryContext queryContext = new QueryContext((Context) options, ResultType.DATA);
        QueryWrapper queryWrapper = new QueryWrapper(query);
        Result<Object> result = executeQuery(queryContext, queryWrapper);
        return (List<E>) result.getRecords();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Page<E> selectPageByQuery(Options options, Object query) {
        QueryContext queryContext = new QueryContext((Context) options, ResultType.COUNT_AND_DATA);
        QueryWrapper queryWrapper = new QueryWrapper(query);
        Result<Object> result = executeQuery(queryContext, queryWrapper);
        return (Page<E>) result.getPage();
    }

    @Override
    public long selectCountByQuery(Options options, Object query) {
        QueryContext queryContext = new QueryContext((Context) options, ResultType.COUNT);
        QueryWrapper queryWrapper = new QueryWrapper(query);
        Result<Object> result = executeQuery(queryContext, queryWrapper);
        return result.getCount();
    }

    @Override
    public Result<Object> executeQuery(QueryContext queryContext, QueryWrapper queryWrapper) {
        resolveQuery(queryContext, queryWrapper);
        Matcher matcher = getRootRepository();
        if (!matcher.matches(queryContext.getContext())) {
            return queryContext.newEmptyResult();
        }
        if (queryContext.isSimpleQuery()) {
            return simpleQueryExecutor.executeQuery(queryContext, queryWrapper);
        } else {
            QueryExecutor queryExecutor = adaptiveQueryExecutor(queryContext, queryWrapper);
            return queryExecutor.executeQuery(queryContext, queryWrapper);
        }
    }

    public void resolveQuery(QueryContext queryContext, QueryWrapper queryWrapper) {
        Map<Class<?>, QueryResolver> classQueryResolverMap = queryTypeResolver.getClassQueryResolverMap();
        QueryResolver queryResolver = classQueryResolverMap.get(queryWrapper.getQuery().getClass());
        Assert.notNull(queryResolver, "No query resolver found!");
        queryContext.setQueryResolver(queryResolver);
        queryResolver.resolve(queryContext, queryWrapper);
    }

    protected QueryExecutor adaptiveQueryExecutor(QueryContext queryContext, QueryWrapper queryWrapper) {
        return stepwiseQueryExecutor;
    }

}
