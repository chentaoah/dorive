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

package com.gitee.dorive.core.impl.handler.joiner;

import com.gitee.dorive.api.entity.core.EntityElement;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.executor.EntityHandler;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.entity.operation.cop.Query;
import com.gitee.dorive.core.impl.binder.WeakBinder;
import com.gitee.dorive.core.impl.factory.OperationFactory;
import com.gitee.dorive.core.impl.resolver.BinderResolver;
import com.gitee.dorive.core.repository.CommonRepository;
import com.gitee.dorive.core.util.ObjectsJoiner;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public abstract class AbstractEntityHandler implements EntityHandler {

    protected CommonRepository repository;

    @Override
    public long handle(Context context, List<Object> entities) {
        ObjectsJoiner objectsJoiner = new ElementObjectsJoiner(entities, repository.isCollection());
        Example example = newExample(context, entities, objectsJoiner);
        if (!example.isEmpty()) {
            OperationFactory operationFactory = repository.getOperationFactory();
            Query query = operationFactory.buildQueryByExample(example);
            query.includeRoot();
            Result<Object> result = repository.executeQuery(context, query);
            objectsJoiner.setAverageSize(result.getRecords().size() / entities.size() + 1);
            handleResult(context, result, objectsJoiner);
            objectsJoiner.join();
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

    private class ElementObjectsJoiner extends ObjectsJoiner {

        public ElementObjectsJoiner(List<Object> entities, boolean collection) {
            super(entities, collection);
        }

        @Override
        protected void doJoin(Object entity, Object object) {
            EntityElement entityElement = repository.getEntityElement();
            Object value = entityElement.getValue(entity);
            if (value == null) {
                entityElement.setValue(entity, object);
            }
        }
    }

    protected abstract Example newExample(Context context, List<Object> entities, ObjectsJoiner objectsJoiner);

    protected abstract void handleResult(Context context, Result<Object> result, ObjectsJoiner objectsJoiner);

}
