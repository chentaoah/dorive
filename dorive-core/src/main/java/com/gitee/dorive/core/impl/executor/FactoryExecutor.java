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

package com.gitee.dorive.core.impl.executor;

import cn.hutool.core.bean.BeanUtil;
import com.gitee.dorive.api.entity.element.EntityEle;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.converter.EntityFactory;
import com.gitee.dorive.core.api.executor.Executor;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.entity.executor.UnionExample;
import com.gitee.dorive.core.entity.operation.Insert;
import com.gitee.dorive.core.entity.operation.Operation;
import com.gitee.dorive.core.entity.operation.Query;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class FactoryExecutor extends AbstractProxyExecutor {

    private EntityEle entityEle;
    private EntityFactory entityFactory;

    public FactoryExecutor(Executor executor, EntityEle entityEle, EntityFactory entityFactory) {
        super(executor);
        this.entityEle = entityEle;
        this.entityFactory = entityFactory;
    }

    @Override
    public Result<Object> executeQuery(Context context, Query query) {
        Result<Object> result = super.executeQuery(context, query);
        Page<Object> page = result.getPage();
        List<Map<String, Object>> recordMaps = result.getRecordMaps();

        List<Object> entities = Collections.emptyList();
        if (recordMaps != null && !recordMaps.isEmpty()) {
            boolean isUnion = query.getExample() instanceof UnionExample;
            entities = reconstitute(context, recordMaps, isUnion);
        }

        if (page != null) {
            page.setRecords(entities);
        }
        result.setRecords(entities);
        result.setRecord(!entities.isEmpty() ? entities.get(0) : null);
        result.setCount(entities.size());
        return result;
    }

    private List<Object> reconstitute(Context context, List<Map<String, Object>> resultMaps, boolean isUnion) {
        List<Object> entities = new ArrayList<>(resultMaps.size());
        for (Map<String, Object> resultMap : resultMaps) {
            Object entity = entityFactory.reconstitute(context, resultMap);
            if (entity != null) {
                if (isUnion) {
                    resultMap.put("$entity", entity);
                }
                entities.add(entity);
            }
        }
        return entities;
    }

    @Override
    public int execute(Context context, Operation operation) {
        Object entity = operation.getEntity();
        if (entity != null) {
            Object persistent = entityFactory.deconstruct(context, entity);
            operation.setEntity(persistent);
        }
        int totalCount = super.execute(context, operation);
        if (operation instanceof Insert) {
            Object persistent = operation.getEntity();
            Object primaryKey = BeanUtil.getFieldValue(persistent, "id");
            entityEle.getPkProxy().setValue(entity, primaryKey);
        }
        operation.setEntity(entity);
        return totalCount;
    }

}
