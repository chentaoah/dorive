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

package com.gitee.dorive.query.impl.repository;

import com.gitee.dorive.api.entity.core.def.RepositoryDef;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.context.Options;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.event.impl.repository.AbstractEventRepository;
import com.gitee.dorive.query.api.QueryHandler;
import com.gitee.dorive.query.api.QueryRepository;
import com.gitee.dorive.query.entity.QueryContext;
import com.gitee.dorive.query.entity.enums.QueryMode;
import com.gitee.dorive.query.entity.enums.ResultType;
import com.gitee.dorive.query.impl.handler.*;
import com.gitee.dorive.query.impl.handler.StepwiseQueryHandler;
import com.gitee.dorive.query.impl.resolver.MergedRepositoryResolver;
import com.gitee.dorive.query.impl.resolver.QueryTypeResolver;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public abstract class AbstractQueryRepository<E, PK> extends AbstractEventRepository<E, PK> implements QueryRepository<E, PK> {

    private MergedRepositoryResolver mergedRepositoryResolver;
    private QueryTypeResolver queryTypeResolver;
    private QueryHandler queryHandler;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();

        RepositoryDef repositoryDef = getRepositoryDef();
        Class<?>[] queries = repositoryDef.getQueries();

        this.mergedRepositoryResolver = new MergedRepositoryResolver(this);
        this.mergedRepositoryResolver.resolve();
        if (queries != null && queries.length > 0) {
            this.queryTypeResolver = new QueryTypeResolver(this);
            this.queryTypeResolver.resolve();
        }

        Map<QueryMode, QueryHandler> queryHandlerMap = new LinkedHashMap<>(4 * 4 / 3 + 1);
        registryQueryHandlers(queryHandlerMap);
        QueryHandler queryHandler = new AdaptiveQueryHandler(queryHandlerMap);
        queryHandler = new SimpleQueryHandler(queryHandler);
        queryHandler = new ContextMatchQueryHandler(this, queryHandler);
        queryHandler = new ExampleQueryHandler(queryHandler);
        queryHandler = new ConfigQueryHandler(this, queryHandler);
        this.queryHandler = queryHandler;
    }

    protected void registryQueryHandlers(Map<QueryMode, QueryHandler> queryHandlerMap) {
        queryHandlerMap.put(QueryMode.STEPWISE, new QueryUnitQueryHandler(new StepwiseQueryHandler()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<E> selectByQuery(Options options, Object query) {
        QueryContext queryContext = new QueryContext((Context) options, query.getClass(), ResultType.DATA);
        queryHandler.handle(queryContext, query);

        Context context = queryContext.getContext();
        Example example = queryContext.getExample();
        Result<Object> result = queryContext.getResult();

        if (queryContext.isAbandoned()) {
            return Collections.emptyList();
        }
        if (result != null) {
            return (List<E>) result.getRecords();
        }
        return selectByExample(context, example);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Page<E> selectPageByQuery(Options options, Object query) {
        QueryContext queryContext = new QueryContext((Context) options, query.getClass(), ResultType.COUNT_AND_DATA);
        queryHandler.handle(queryContext, query);

        Context context = queryContext.getContext();
        Example example = queryContext.getExample();
        Result<Object> result = queryContext.getResult();

        if (queryContext.isAbandoned()) {
            return (Page<E>) example.getPage();
        }
        if (result != null) {
            return (Page<E>) result.getPage();
        }
        return selectPageByExample(context, example);
    }

    @Override
    public long selectCountByQuery(Options options, Object query) {
        QueryContext queryContext = new QueryContext((Context) options, query.getClass(), ResultType.COUNT);
        queryHandler.handle(queryContext, query);

        Context context = queryContext.getContext();
        Example example = queryContext.getExample();
        Result<Object> result = queryContext.getResult();

        if (queryContext.isAbandoned()) {
            return 0L;
        }
        if (result != null) {
            return result.getCount();
        }
        return selectCountByExample(context, example);
    }
}
