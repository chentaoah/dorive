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

package com.gitee.dorive.core.impl.handler.qry;

import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.executor.v1.api.EntityHandler;
import com.gitee.dorive.joiner.v1.api.EntityJoiner;
import com.gitee.dorive.base.v1.core.entity.qry.Example;
import com.gitee.dorive.base.v1.core.entity.op.Result;
import com.gitee.dorive.base.v1.core.entity.cop.Query;
import com.gitee.dorive.binder.v1.impl.binder.WeakBinder;
import com.gitee.dorive.executor.v1.impl.factory.OperationFactory;
import com.gitee.dorive.core.impl.repository.ProxyRepository;
import com.gitee.dorive.core.impl.resolver.BinderResolver;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public abstract class AbstractEntityHandler implements EntityHandler {

    protected final ProxyRepository repository;
    protected final EntityJoiner entityJoiner;

    @Override
    public long handle(Context context, List<Object> entities) {
        Example example = newExample(context, entities);
        if (!example.isEmpty()) {
            OperationFactory operationFactory = repository.getOperationFactory();
            Query query = operationFactory.buildQueryByExample(example);
            query.includeRoot();
            Result<Object> result = repository.executeQuery(context, query);
            entityJoiner.setCollectionSize(result.getRecords().size() / entities.size() + 1);
            handleResult(context, result);
            entityJoiner.join(entities);
            return result.getCount();
        }
        return 0L;
    }

    protected void appendFilterCriteria(Context context, Example example) {
        if (example != null && !example.isEmpty()) {
            BinderResolver binderResolver = repository.getBinderResolver();
            List<WeakBinder> weakBinders = binderResolver.getWeakBinders();
            for (WeakBinder weakBinder : weakBinders) {
                Object boundValue = weakBinder.input(context, null);
                if (boundValue != null) {
                    String fieldName = weakBinder.getFieldName();
                    example.eq(fieldName, boundValue);
                }
            }
            binderResolver.appendFilterValue(context, example);
        }
    }

    protected abstract Example newExample(Context context, List<Object> entities);

    protected abstract void handleResult(Context context, Result<Object> result);

}
