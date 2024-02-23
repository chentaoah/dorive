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
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.query.entity.enums.ResultType;
import com.gitee.dorive.query.impl.resolver.QueryResolver;
import lombok.Data;

import java.util.Collections;
import java.util.Map;

@Data
public class QueryContext {

    private Context context;
    private ResultType resultType;
    private QueryResolver queryResolver;
    private Map<String, Example> exampleMap;
    private Example example;

    public QueryContext(Context context, ResultType resultType) {
        this.context = context;
        this.resultType = resultType;
    }

    public boolean isSimpleQuery() {
        return exampleMap.size() == 1 && exampleMap.containsKey("/");
    }

    public boolean isNeedCount() {
        return resultType == ResultType.COUNT_AND_DATA || resultType == ResultType.COUNT;
    }

    public Result<Object> newEmptyResult() {
        if (resultType == ResultType.COUNT_AND_DATA) {
            return new Result<>(example.getPage());

        } else if (resultType == ResultType.DATA) {
            return new Result<>(Collections.emptyList());

        } else if (resultType == ResultType.COUNT) {
            return new Result<>(0L);
        }
        throw new RuntimeException("Unsupported type!");
    }

}
