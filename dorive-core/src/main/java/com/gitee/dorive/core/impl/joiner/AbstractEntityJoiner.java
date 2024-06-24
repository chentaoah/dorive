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

import com.gitee.dorive.api.ele.EntityElement;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.executor.EntityJoiner;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.impl.binder.WeakBinder;
import com.gitee.dorive.core.impl.resolver.BinderResolver;
import com.gitee.dorive.core.repository.CommonRepository;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractEntityJoiner implements EntityJoiner {

    protected CommonRepository repository;
    protected Map<Integer, String> rootIndex;
    protected Set<String> keys;
    protected Map<String, Object> recordIndex;
    protected int averageSize;

    public AbstractEntityJoiner(CommonRepository repository, int entitiesSize) {
        this.repository = repository;
        int size = entitiesSize * 4 / 3 + 1;
        this.rootIndex = new LinkedHashMap<>(size);
        this.keys = new LinkedHashSet<>(size);
        this.recordIndex = new LinkedHashMap<>(size);
    }

    protected void appendFilterCriteria(Context context, Example example) {
        if (example == null || example.isEmpty()) {
            return;
        }
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

    protected void addToRootIndex(Object entity, String key) {
        if (entity == null || StringUtils.isBlank(key)) {
            return;
        }
        rootIndex.put(System.identityHashCode(entity), key);
        keys.add(key);
    }

    @Override
    public void join(Context context, List<Object> entities, Result<Object> result) {
        List<Object> records = result.getRecords();
        averageSize = records.size() / entities.size() + 1;
        buildRecordIndex(context, entities, result);
        joinMany(entities);
    }

    @SuppressWarnings("unchecked")
    protected void addToRecordIndex(String key, Object entity) {
        if (StringUtils.isBlank(key) || entity == null) {
            return;
        }
        if (repository.isCollection()) {
            Collection<Object> collection = (Collection<Object>) recordIndex.computeIfAbsent(key, k -> new ArrayList<>(averageSize));
            collection.add(entity);
        } else {
            recordIndex.putIfAbsent(key, entity);
        }
    }

    protected void joinMany(List<Object> entities) {
        for (Object entity : entities) {
            String key = rootIndex.get(System.identityHashCode(entity));
            if (key != null) {
                Object object = recordIndex.get(key);
                joinOne(entity, object);
            }
        }
    }

    protected void joinOne(Object entity, Object object) {
        if (entity == null || object == null) {
            return;
        }
        EntityElement entityElement = repository.getEntityElement();
        Object value = entityElement.getValue(entity);
        if (value == null) {
            entityElement.setValue(entity, object);
        }
    }

    protected abstract void buildRecordIndex(Context context, List<Object> entities, Result<Object> result);

}
