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

package com.gitee.dorive.query2.v1.impl.custom;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Assert;
import com.gitee.dorive.base.v1.common.entity.QueryDefinition;
import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.base.v1.core.api.Options;
import com.gitee.dorive.base.v1.core.entity.op.Result;
import com.gitee.dorive.base.v1.core.entity.qry.Example;
import com.gitee.dorive.base.v1.core.entity.qry.InnerExample;
import com.gitee.dorive.base.v1.core.entity.qry.OrderBy;
import com.gitee.dorive.base.v1.core.entity.qry.Page;
import com.gitee.dorive.base.v1.mybatis.api.MethodInvoker;
import com.gitee.dorive.base.v1.mybatis.entity.EntityStoreInfo;
import com.gitee.dorive.base.v1.query.api.QueryExecutor;
import com.gitee.dorive.base.v1.repository.impl.AbstractRepository;
import com.gitee.dorive.query2.v1.entity.QueryConfig;
import com.gitee.dorive.query2.v1.impl.core.ExampleResolver;
import com.gitee.dorive.query2.v1.impl.core.QueryConfigResolver;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class CustomQueryExecutor implements QueryExecutor {

    private final QueryConfigResolver queryConfigResolver;
    private final String primaryKey;
    private final EntityStoreInfo entityStoreInfo;
    private final AbstractRepository<Object, Object> repository;

    @Override
    public List<Object> selectByQuery(Options options, Object query) {
        Result<Object> result = executeQuery((Context) options, query);
        return result.getRecords();
    }

    @Override
    public Page<Object> selectPageByQuery(Options options, Object query) {
        Result<Object> result = executeQuery((Context) options, query);
        return result.getPage();
    }

    @Override
    public long selectCountByQuery(Options options, Object query) {
        return executeCount((Context) options, query);
    }

    @SuppressWarnings("unchecked")
    private Result<Object> executeQuery(Context context, Object query) {
        QueryConfig queryConfig = queryConfigResolver.findQueryConfig(query.getClass());
        Assert.notNull(queryConfig, "No query config found!");
        ExampleResolver exampleResolver = queryConfig.getExampleResolver();

        QueryDefinition queryDefinition = exampleResolver.getQueryDefinition();
        String method = queryDefinition.getQueryDef().getMethod();

        OrderBy orderBy = exampleResolver.newOrderBy(query);
        Page<Object> page = exampleResolver.newPage(query);

        Map<String, Object> params = new HashMap<>(8);
        params.put("context", context.getAttachments());
        params.put("query", query);
        params.put("orderBy", orderBy);
        params.put("page", page);

        Map<String, MethodInvoker> selectMethodMap = entityStoreInfo.getSelectMethodMap();
        MethodInvoker methodInvoker = selectMethodMap.get(method);
        Assert.notNull(methodInvoker, "The method invoker does not exist!");
        List<Object> ids = (List<Object>) methodInvoker.invoke(params);

        // 查询实体
        List<Object> entities = Collections.emptyList();
        if (!ids.isEmpty()) {
            Example newExample = new InnerExample().in(primaryKey, ids);
            newExample.setOrderBy(orderBy);
            entities = repository.selectByExample(context, newExample);
        }

        if (page != null) {
            page.setRecords(entities);
            return new Result<>(page);
        } else {
            return new Result<>(entities);
        }
    }

    private long executeCount(Context context, Object query) {
        QueryConfig queryConfig = queryConfigResolver.findQueryConfig(query.getClass());
        Assert.notNull(queryConfig, "No query config found!");
        ExampleResolver exampleResolver = queryConfig.getExampleResolver();

        QueryDefinition queryDefinition = exampleResolver.getQueryDefinition();
        String countMethod = queryDefinition.getQueryDef().getCountMethod();

        OrderBy orderBy = exampleResolver.newOrderBy(query);
        Page<Object> page = exampleResolver.newPage(query);

        Map<String, Object> params = new HashMap<>(8);
        params.put("context", context.getAttachments());
        params.put("query", query);
        params.put("orderBy", orderBy);
        params.put("page", page);

        Map<String, MethodInvoker> selectMethodMap = entityStoreInfo.getSelectMethodMap();
        MethodInvoker methodInvoker = selectMethodMap.get(countMethod);
        Assert.notNull(methodInvoker, "The method invoker does not exist!");
        return Convert.convert(Long.class, methodInvoker.invoke(params));
    }

}
