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

import com.gitee.dorive.api.annotation.Repository;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.context.Options;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.event.repository.AbstractEventRepository;
import com.gitee.dorive.query.api.QueryExecutor;
import com.gitee.dorive.query.api.QueryRepository;
import com.gitee.dorive.query.entity.QueryContext;
import com.gitee.dorive.query.entity.def.QueryScanDef;
import com.gitee.dorive.query.entity.enums.ResultType;
import com.gitee.dorive.query.impl.executor.StepwiseQueryExecutor;
import com.gitee.dorive.query.impl.resolver.MergedRepositoryResolver;
import com.gitee.dorive.query.impl.resolver.QueryTypeResolver;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.util.List;

@Getter
@Setter
public abstract class AbstractQueryRepository<E, PK> extends AbstractEventRepository<E, PK> implements QueryRepository<E, PK>, QueryExecutor {

    private QueryScanDef queryScanDef;
    private MergedRepositoryResolver mergedRepositoryResolver;
    private QueryTypeResolver queryTypeResolver;
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
            queryTypeResolver.resolve();
            this.stepwiseQueryExecutor = new StepwiseQueryExecutor(this);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<E> selectByQuery(Options options, Object query) {
        QueryContext queryContext = new QueryContext((Context) options, query, ResultType.DATA);
        Result<Object> result = executeQuery(queryContext);
        return (List<E>) result.getRecords();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Page<E> selectPageByQuery(Options options, Object query) {
        QueryContext queryContext = new QueryContext((Context) options, query, ResultType.COUNT_AND_DATA);
        Result<Object> result = executeQuery(queryContext);
        return (Page<E>) result.getPage();
    }

    @Override
    public long selectCountByQuery(Options options, Object query) {
        QueryContext queryContext = new QueryContext((Context) options, query, ResultType.COUNT);
        Result<Object> result = executeQuery(queryContext);
        return result.getCount();
    }

    @Override
    public Result<Object> executeQuery(QueryContext queryContext) {
        QueryExecutor queryExecutor = adaptiveQueryExecutor(queryContext);
        return queryExecutor.executeQuery(queryContext);
    }

    protected QueryExecutor adaptiveQueryExecutor(QueryContext queryContext) {
        return stepwiseQueryExecutor;
    }

}
