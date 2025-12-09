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

import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import com.gitee.dorive.binder.v1.impl.binder.ValueRouteBinder;
import com.gitee.dorive.binder.v1.impl.resolver.BinderResolver;
import com.gitee.dorive.base.v1.executor.api.EntityHandler;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class ValueFilterEntityHandler implements EntityHandler {

    private RepositoryItem repository;
    private EntityHandler entityHandler;

    @Override
    public long handle(Context context, List<Object> entities) {
        List<Object> subEntities = filterByValueRouteBinders(context, entities);
        return !subEntities.isEmpty() ? entityHandler.handle(context, subEntities) : 0L;
    }

    private List<Object> filterByValueRouteBinders(Context context, List<Object> entities) {
        BinderResolver binderResolver = repository.getBinderResolver();
        List<ValueRouteBinder> valueRouteBinders = binderResolver.getValueRouteBinders();
        if (valueRouteBinders.isEmpty()) {
            return entities;
        }
        List<Object> subEntities = new ArrayList<>(entities.size());
        for (Object entity : entities) {
            boolean isValueEqual = true;
            for (ValueRouteBinder valueRouteBinder : valueRouteBinders) {
                Object fieldValue = valueRouteBinder.getFieldValue(context, null);
                Object boundValue = valueRouteBinder.getBoundValue(context, entity);
                boundValue = valueRouteBinder.input(context, boundValue);
                if (!fieldValue.equals(boundValue)) {
                    isValueEqual = false;
                    break;
                }
            }
            if (isValueEqual) {
                subEntities.add(entity);
            }
        }
        return subEntities;
    }

}
