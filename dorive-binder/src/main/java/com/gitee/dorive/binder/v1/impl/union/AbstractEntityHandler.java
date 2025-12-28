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

package com.gitee.dorive.binder.v1.impl.union;

import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.base.v1.core.entity.cop.Query;
import com.gitee.dorive.base.v1.core.entity.op.Result;
import com.gitee.dorive.base.v1.core.entity.qry.Example;
import com.gitee.dorive.base.v1.core.impl.OperationFactory;
import com.gitee.dorive.base.v1.executor.api.EntityHandler;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public abstract class AbstractEntityHandler implements EntityHandler {

    protected final RepositoryItem repository;
    protected final KeyValueJoiner keyValueJoiner;

    @Override
    public long handle(Context context, List<Object> entities) {
        Example example = newExample(context, entities);
        if (!example.isEmpty()) {
            OperationFactory operationFactory = repository.getOperationFactory();
            Query query = operationFactory.buildQueryByExample(example);
            query.includeRoot();
            Result<Object> result = repository.executeQuery(context, query);
            keyValueJoiner.setCollectionSize(result.getRecords().size() / entities.size() + 1);
            handleResult(context, result);
            keyValueJoiner.join(entities);
            return result.getCount();
        }
        return 0L;
    }

    protected abstract Example newExample(Context context, List<Object> entities);

    protected abstract void handleResult(Context context, Result<Object> result);

}
