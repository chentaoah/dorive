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

package com.gitee.dorive.binder.v1.impl.handler;

import com.gitee.dorive.base.v1.binder.api.BinderExecutor;
import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.base.v1.core.entity.cop.Query;
import com.gitee.dorive.base.v1.core.entity.op.Result;
import com.gitee.dorive.base.v1.core.entity.qry.Example;
import com.gitee.dorive.base.v1.core.impl.OperationFactory;
import com.gitee.dorive.base.v1.executor.api.EntityHandler;
import com.gitee.dorive.base.v1.joiner.api.EntityJoiner;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import com.gitee.dorive.binder.v1.api.ExampleBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DefaultEntityHandler implements EntityHandler {

    private final RepositoryItem repository;

    @Override
    public long handle(Context context, List<Object> entities) {
        OperationFactory operationFactory = repository.getOperationFactory();
        BinderExecutor binderExecutor = repository.getBinderExecutor();
        ExampleBuilder exampleBuilder = repository.getProperty(ExampleBuilder.class);
        EntityJoiner entityJoiner = repository.getProperty(EntityJoiner.class);

        Example example = exampleBuilder.newExample(context, entities);
        binderExecutor.appendFilterCriteria(context, example);
        if (example.isEmpty()) {
            return 0L;
        }
        Query query = operationFactory.buildQueryByExample(example);
        query.includeRoot();
        Result<Object> result = repository.executeQuery(context, query);
        entityJoiner.join(context, entities, result.getRecords());
        return result.getCount();
    }

}
