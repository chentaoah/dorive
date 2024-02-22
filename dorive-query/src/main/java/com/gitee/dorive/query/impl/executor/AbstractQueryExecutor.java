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

import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.query.api.QueryExecutor;
import com.gitee.dorive.query.entity.QueryContext;
import com.gitee.dorive.query.entity.QueryWrapper;
import com.gitee.dorive.query.entity.enums.ResultType;
import com.gitee.dorive.query.repository.AbstractQueryRepository;

import java.util.List;

public abstract class AbstractQueryExecutor implements QueryExecutor {

    @Override
    public Result<Object> executeQuery(QueryContext queryContext, QueryWrapper queryWrapper) {
        AbstractQueryRepository<?, ?> repository = queryContext.getRepository();
        Context context = queryContext.getContext();
        ResultType resultType = queryContext.getResultType();
        Example example = queryContext.getExample();
        if (resultType == ResultType.COUNT_AND_DATA) {
            Page<?> page = repository.selectPageByExample(context, example);
            return new Result<Object>(page);

        } else if (resultType == ResultType.DATA) {
            List<?> entities = repository.selectByExample(context, example);
            return new Result<Object>(entities);

        } else if (resultType == ResultType.COUNT) {
            long count = repository.selectCountByExample(context, example);
            return new Result<>(count);
        }
        return queryContext.newEmptyResult();
    }

}
