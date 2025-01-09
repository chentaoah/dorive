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
import com.gitee.dorive.query.api.QueryHandler;
import com.gitee.dorive.query.entity.QueryContext;
import com.gitee.dorive.query.impl.resolver.QueryExampleResolver;
import com.gitee.dorive.query.impl.resolver.QueryTypeResolver;
import com.gitee.dorive.query.repository.AbstractQueryRepository;
import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
public class ExampleQueryHandler implements QueryHandler {

    private final AbstractQueryRepository<?, ?> repository;
    private final QueryHandler queryHandler;

    @Override
    public void handle(QueryContext queryContext, Object query) {
        QueryTypeResolver queryTypeResolver = repository.getQueryTypeResolver();
        Map<Class<?>, QueryExampleResolver> classQueryExampleResolverMap = queryTypeResolver.getClassQueryExampleResolverMap();

        Class<?> queryType = queryContext.getQueryType();
        QueryExampleResolver queryExampleResolver = classQueryExampleResolverMap.get(queryType);

        Assert.notNull(queryExampleResolver, "No query resolver found!");
        queryContext.setQueryExampleResolver(queryExampleResolver);
        queryContext.setMethod(queryExampleResolver.getMethod());

        Map<String, Example> exampleMap = queryExampleResolver.resolve(query);
        queryContext.setExampleMap(exampleMap);
        queryContext.setExample(exampleMap.get("/"));

        if (queryHandler != null) {
            queryHandler.handle(queryContext, query);
        }
    }

}
