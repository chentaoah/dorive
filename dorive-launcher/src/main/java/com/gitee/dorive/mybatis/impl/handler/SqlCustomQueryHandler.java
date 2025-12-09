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

package com.gitee.dorive.mybatis.impl.handler;

import cn.hutool.core.lang.Assert;
import com.gitee.dorive.base.v1.common.api.MethodInvoker;
import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.base.v1.core.entity.Example;
import com.gitee.dorive.base.v1.core.entity.OrderBy;
import com.gitee.dorive.base.v1.core.entity.Page;
import com.gitee.dorive.mybatis.entity.common.EntityStoreInfo;
import com.gitee.dorive.query.entity.QueryContext;
import com.gitee.dorive.query.entity.QueryUnit;
import com.gitee.dorive.query.impl.repository.AbstractQueryRepository;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class SqlCustomQueryHandler extends SqlBuildQueryHandler {

    private final EntityStoreInfo entityStoreInfo;

    public SqlCustomQueryHandler(AbstractQueryRepository<?, ?> repository, EntityStoreInfo entityStoreInfo) {
        super(repository);
        this.entityStoreInfo = entityStoreInfo;
    }

    @Override
    protected void processAttachment(QueryContext queryContext, Map<String, QueryUnit> queryUnitMap, QueryUnit queryUnit) {
        // ignore
    }

    @Override
    @SuppressWarnings("unchecked")
    public void doHandle(QueryContext queryContext, Object query) {
        Context context = queryContext.getContext();
        String primaryKey = queryContext.getPrimaryKey();
        String method = queryContext.getMethod();
        Example example = queryContext.getExample();

        OrderBy orderBy = example.getOrderBy();
        Page<Object> page = example.getPage();

        Map<String, Object> params = new HashMap<>(8);
        params.put("context", context.getAttachments());
        params.put("query", query);
        params.put("orderBy", orderBy);
        params.put("page", page);

        Map<String, MethodInvoker> selectMethodMap = entityStoreInfo.getSelectMethodMap();
        MethodInvoker methodInvoker = selectMethodMap.get(method);
        Assert.notNull(methodInvoker, "The method invoker does not exist!");
        List<Object> ids = (List<Object>) methodInvoker.invoke(params);
        if (!ids.isEmpty()) {
            example.in(primaryKey, ids);
            doQuery(queryContext, ids);
        } else {
            queryContext.setAbandoned(true);
        }
    }
}
