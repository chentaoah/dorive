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
import com.gitee.dorive.core.api.common.EntityFactory;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.executor.Executor;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.entity.executor.UnionExample;
import com.gitee.dorive.core.entity.operation.Insert;
import com.gitee.dorive.core.entity.operation.Operation;
import com.gitee.dorive.core.entity.operation.Query;
import com.gitee.dorive.core.impl.executor.AbstractExecutor;
import com.gitee.dorive.spring.boot.starter.entity.QueryResult;
import com.gitee.dorive.core.entity.executor.MultiResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FactoryExecutor extends AbstractExecutor {

    private EntityEle entityEle;
    private EntityFactory entityFactory;
    private Executor executor;

    @Override
    public Result<Object> executeQuery(Context context, Query query) {
        Result<Object> result = executor.executeQuery(context, query);
        Example example = query.getExample();
        QueryResult queryResult = (QueryResult) result.getRecord();

        List<Map<String, Object>> resultMaps = queryResult.getResultMaps();
        if (resultMaps != null) {
            List<Object> entities = reconstitute(context, resultMaps);
            if (example instanceof UnionExample) {
                return new MultiResult(resultMaps, entities);
            } else {
                return new Result<>(entities);
            }
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
        int totalCount = executor.execute(context, operation);
        if (operation instanceof Insert) {
            Object persistent = operation.getEntity();
            Object primaryKey = BeanUtil.getFieldValue(persistent, "id");
            entityEle.getPkProxy().setValue(entity, primaryKey);
        }
        operation.setEntity(entity);
        return totalCount;
    }

}
