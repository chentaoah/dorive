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

package com.gitee.dorive.query.v1.impl.executor;

import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.base.v1.core.api.Options;
import com.gitee.dorive.base.v1.core.entity.op.Result;
import com.gitee.dorive.base.v1.core.entity.qry.Example;
import com.gitee.dorive.base.v1.core.entity.qry.Page;
import com.gitee.dorive.base.v1.query.api.QueryExecutor;
import com.gitee.dorive.base.v1.repository.impl.AbstractRepository;
import com.gitee.dorive.query.v1.api.QueryHandler;
import com.gitee.dorive.query.v1.entity.QueryContext;
import com.gitee.dorive.query.v1.enums.ResultType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
@AllArgsConstructor
public class DefaultQueryExecutor implements QueryExecutor {

    private final QueryHandler queryHandler;
    private final AbstractRepository<Object, Object> repository;

    @Override
    public List<Object> selectByQuery(Options options, Object query) {
        QueryContext queryContext = new QueryContext((Context) options, query.getClass(), ResultType.DATA);
        queryHandler.handle(queryContext, query);

        Context context = queryContext.getContext();
        Example example = queryContext.getExample();
        Result<Object> result = queryContext.getResult();

        if (queryContext.isAbandoned()) {
            return Collections.emptyList();
        }
        if (result != null) {
            return result.getRecords();
        }
        return repository.selectByExample(context, example);
    }

    @Override
    public Page<Object> selectPageByQuery(Options options, Object query) {
        QueryContext queryContext = new QueryContext((Context) options, query.getClass(), ResultType.COUNT_AND_DATA);
        queryHandler.handle(queryContext, query);

        Context context = queryContext.getContext();
        Example example = queryContext.getExample();
        Result<Object> result = queryContext.getResult();

        if (queryContext.isAbandoned()) {
            return example.getPage();
        }
        if (result != null) {
            return result.getPage();
        }
        return repository.selectPageByExample(context, example);
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
        return repository.selectCountByExample(context, example);
    }

}
