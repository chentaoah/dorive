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
import com.gitee.dorive.core.api.executor.EntityFactory;
import com.gitee.dorive.core.api.executor.Executor;
import com.gitee.dorive.core.entity.executor.*;
import com.gitee.dorive.core.entity.operation.Insert;
import com.gitee.dorive.core.entity.operation.Operation;
import com.gitee.dorive.core.entity.operation.Query;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.*;

@Data
@EqualsAndHashCode(callSuper = false)
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
            Example example = query.getExample();
            if (example instanceof UnionExample) {
                entities = reconstituteWithoutDuplicate(context, recordMaps);
            } else {
                entities = reconstitute(context, recordMaps);
            }
        }

        if (page != null) {
            page.setRecords(entities);
        }
        result.setRecords(entities);
        result.setRecord(!entities.isEmpty() ? entities.get(0) : null);
        result.setCount(entities.size());
        return result;
    }

    private List<Object> reconstituteWithoutDuplicate(Context context, List<Map<String, Object>> resultMaps) {
        int size = resultMaps.size();
        List<Object> entities = new ArrayList<>(size);
        Map<String, Object> existEntityMap = new LinkedHashMap<>(size * 4 / 3 + 1);
        for (Map<String, Object> resultMap : resultMaps) {
            Object id = resultMap.get("id");
            Object entity = null;
            if (id != null) {
                entity = existEntityMap.get(String.valueOf(id));
            }
            if (entity == null) {
                entity = entityFactory.reconstitute(context, resultMap);
                if (id != null) {
                    existEntityMap.put(String.valueOf(id), entity);
                }
                entities.add(entity);
            }
            if (entity != null) {
                resultMap.put("$entity", entity);
            }
        }
        return entities;
    }

    private List<Object> reconstitute(Context context, List<Map<String, Object>> resultMaps) {
        List<Object> entities = new ArrayList<>(resultMaps.size());
        for (Map<String, Object> resultMap : resultMaps) {
            Object entity = entityFactory.reconstitute(context, resultMap);
            if (entity != null) {
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
