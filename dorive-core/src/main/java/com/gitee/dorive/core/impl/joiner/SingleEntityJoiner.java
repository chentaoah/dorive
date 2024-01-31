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

package com.gitee.dorive.core.impl.joiner;

import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.InnerExample;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.impl.binder.PropertyBinder;
import com.gitee.dorive.core.repository.CommonRepository;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SingleEntityJoiner extends AbstractEntityJoiner {

    private PropertyBinder binder;

    public SingleEntityJoiner(CommonRepository repository, int entitiesSize) {
        super(repository, entitiesSize);
        this.binder = repository.getRootBinders().get(0);
    }

    @Override
    public Example newExample(Context context, List<Object> entities) {
        Example example = new InnerExample();
        List<Object> boundValues = collectBoundValues(context, entities);
        if (!boundValues.isEmpty()) {
            String fieldName = binder.getFieldName();
            if (boundValues.size() == 1) {
                example.eq(fieldName, boundValues.get(0));
            } else {
                example.in(fieldName, boundValues);
            }
        }
        appendContext(context, example);
        return example;
    }

    private List<Object> collectBoundValues(Context context, List<Object> entities) {
        List<Object> boundValues = new ArrayList<>(entities.size());
        for (Object entity : entities) {
            Object boundValue = binder.getBoundValue(context, entity);
            if (boundValue == null) {
                continue;
            }
            boundValue = binder.input(context, boundValue);
            if (boundValue == null) {
                continue;
            }
            String key = boundValue.toString();
            if (!keys.contains(key)) {
                boundValues.add(boundValue);
            }
            addToRootIndex(entity, key);
        }
        return boundValues;
    }

    @Override
    protected void addToRecordIndex(Context context, List<Object> entities, Result<Object> result) {
        List<Object> records = result.getRecords();
        for (Object entity : records) {
            Object fieldValue = binder.getFieldValue(context, entity);
            if (fieldValue == null) {
                continue;
            }
            String key = fieldValue.toString();
            addToRecordIndex(key, entity);
        }
    }

}
