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

package com.gitee.dorive.sql.impl.handler;

import cn.hutool.core.util.ReflectUtil;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.entity.common.EntityStoreInfo;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.query.api.QueryHandler;
import com.gitee.dorive.query.entity.QueryContext;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class SqlCustomQueryHandler implements QueryHandler {

    private final EntityStoreInfo entityStoreInfo;

    public SqlCustomQueryHandler(EntityStoreInfo entityStoreInfo) {
        this.entityStoreInfo = entityStoreInfo;
    }

    @Override
    public void handle(QueryContext queryContext, Object query) {
        Context context = queryContext.getContext();
        String primaryKey = queryContext.getPrimaryKey();
        String method = queryContext.getMethod();
        Example example = queryContext.getExample();

        Object mapper = entityStoreInfo.getMapper();
        Map<String, Method> selectMethodMap = entityStoreInfo.getSelectMethodMap();
        Method selectMethod = selectMethodMap.get(method);
        Map<String, Object> attachments = context.getAttachments();

        int parameterCount = selectMethod.getParameterCount();
        List<Object> primaryKeys = Collections.emptyList();
        if (parameterCount == 1) {
            primaryKeys = ReflectUtil.invoke(mapper, selectMethod, query);

        } else if (parameterCount == 2) {
            primaryKeys = ReflectUtil.invoke(mapper, selectMethod, attachments, query);
        }
        if (!primaryKeys.isEmpty()) {
            example.in(primaryKey, primaryKeys);
        } else {
            queryContext.setAbandoned(true);
        }
    }
}
