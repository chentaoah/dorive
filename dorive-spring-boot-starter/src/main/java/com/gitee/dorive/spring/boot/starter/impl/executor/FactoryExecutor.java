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

package com.gitee.dorive.spring.boot.starter.impl.executor;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gitee.dorive.api.entity.element.EntityEle;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.executor.EntityFactory;
import com.gitee.dorive.core.api.executor.Executor;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.MultiResult;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.entity.executor.UnionExample;
import com.gitee.dorive.core.entity.operation.Insert;
import com.gitee.dorive.core.entity.operation.Operation;
import com.gitee.dorive.core.entity.operation.Query;
import com.gitee.dorive.core.impl.executor.AbstractProxyExecutor;
import com.gitee.dorive.spring.boot.starter.entity.QueryResult;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        Example example = query.getExample();
        QueryResult queryResult = (QueryResult) result.getRecord();

        List<Map<String, Object>> resultMaps = queryResult.getResultMaps();
        if (resultMaps != null) {
            List<Object> entities;
            if (example instanceof UnionExample) {
                entities = reconstituteWithoutDuplicate(context, resultMaps);
            } else {
                entities = reconstitute(context, resultMaps);
            }
            return new MultiResult(resultMaps, entities);
        }

        Page<Map<String, Object>> queryPage = queryResult.getPage();
        if (queryPage != null) {
            com.gitee.dorive.core.entity.executor.Page<Object> page = example.getPage();
            page.setTotal(queryPage.getTotal());
            List<Object> entities = reconstitute(context, queryPage.getRecords());
            page.setRecords(entities);
            return new Result<>(page);
        }

        return new Result<>(Collections.emptyList());
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
