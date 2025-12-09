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

package com.gitee.dorive.core.impl.executor.unit;

import cn.hutool.core.bean.BeanUtil;
import com.gitee.dorive.base.v1.common.entity.EntityElement;
import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.core.api.executor.Executor;
import com.gitee.dorive.core.api.factory.EntityFactory;
import com.gitee.dorive.base.v1.core.entity.Page;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.entity.operation.EntityOp;
import com.gitee.dorive.core.entity.operation.Operation;
import com.gitee.dorive.core.entity.operation.cop.ConditionUpdate;
import com.gitee.dorive.core.entity.operation.cop.Query;
import com.gitee.dorive.core.entity.operation.eop.Insert;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class FactoryExecutor extends AbstractProxyExecutor {

    private EntityElement entityElement;
    private String pkOfPersistentObj;
    private EntityFactory entityFactory;

    public FactoryExecutor(Executor executor, EntityElement entityElement, String pkOfPersistentObj, EntityFactory entityFactory) {
        super(executor);
        this.entityElement = entityElement;
        this.pkOfPersistentObj = pkOfPersistentObj;
        this.entityFactory = entityFactory;
    }

    @Override
    public Result<Object> executeQuery(Context context, Query query) {
        Result<Object> result = super.executeQuery(context, query);
        Page<Object> page = result.getPage();
        List<Map<String, Object>> recordMaps = result.getRecordMaps();

        List<Object> entities = Collections.emptyList();
        if (recordMaps != null && !recordMaps.isEmpty()) {
            entities = entityFactory.reconstitute(context, recordMaps);
        }

        if (page != null) {
            page.setRecords(entities);
        }
        result.setRecords(entities);
        result.setRecord(!entities.isEmpty() ? entities.get(0) : null);
        result.setCount(entities.size());
        return result;
    }

    @Override
    public int execute(Context context, Operation operation) {
        if (operation instanceof EntityOp) {
            EntityOp entityOp = (EntityOp) operation;
            List<?> entities = entityOp.getEntities();
            List<Object> persistentObjs = entityFactory.deconstruct(context, entities);
            entityOp.setEntities(persistentObjs);
            int totalCount = super.execute(context, operation);
            entityOp.setEntities(entities);

            if (operation instanceof Insert) {
                for (int index = 0; index < entities.size(); index++) {
                    Object entity = entities.get(index);
                    Object persistentObj = persistentObjs.get(index);
                    Object primaryKey = BeanUtil.getFieldValue(persistentObj, pkOfPersistentObj);
                    if (primaryKey != null) {
                        entityElement.setPrimaryKey(entity, primaryKey);
                    }
                }
            }
            return totalCount;

        } else if (operation instanceof ConditionUpdate) {
            ConditionUpdate conditionUpdate = (ConditionUpdate) operation;
            Object entity = conditionUpdate.getEntity();
            if (entity != null) {
                List<Object> persistentObjs = entityFactory.deconstruct(context, Collections.singletonList(entity));
                conditionUpdate.setEntity(persistentObjs.get(0));
                int totalCount = super.execute(context, operation);
                conditionUpdate.setEntity(entity);
                return totalCount;
            }
        }
        return super.execute(context, operation);
    }

}
