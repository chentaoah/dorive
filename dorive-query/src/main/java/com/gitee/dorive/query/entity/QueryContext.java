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

package com.gitee.dorive.query.entity;

import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.entity.context.AbstractProxyContext;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.query.entity.enums.ResultType;
import com.gitee.dorive.query.impl.resolver.QueryExampleResolver;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class QueryContext extends AbstractProxyContext {
    private Class<?> queryType;
    private ResultType resultType;
    private boolean abandoned;

    private QueryExampleResolver queryExampleResolver;
    private Map<String, Example> exampleMap;
    private Example example;

    private List<MergedRepository> mergedRepositories;
    private Map<String, QueryUnit> queryUnitMap;
    private QueryUnit queryUnit;

    private List<Object> args = new ArrayList<>(8);
    private Result<Object> result;

    public QueryContext(Context context, Class<?> queryType, ResultType resultType) {
        super(context);
        this.queryType = queryType;
        this.resultType = resultType;
    }

    public boolean isNeedCount() {
        return resultType == ResultType.COUNT_AND_DATA || resultType == ResultType.COUNT;
    }

    public boolean isAbandoned() {
        return abandoned || (queryUnit != null && queryUnit.isAbandoned());
    }
}
