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

package com.gitee.dorive.mybatis.plus.impl;

import cn.hutool.core.util.ReflectUtil;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.entity.common.EntityStoreInfo;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.query.entity.QueryContext;
import com.gitee.dorive.query.impl.handler.AbstractQueryUnitQueryHandler;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class SqlCustomQueryHandler extends AbstractQueryUnitQueryHandler {

    private final EntityStoreInfo entityStoreInfo;

    public SqlCustomQueryHandler(EntityStoreInfo entityStoreInfo) {
        this.entityStoreInfo = entityStoreInfo;
    }

    @Override
    protected void doHandle(QueryContext queryContext, Object query) {
        Context context = queryContext.getContext();
        String primaryKey = queryContext.getPrimaryKey();
        String method = queryContext.getMethod();
        Example example = queryContext.getExample();

        Object mapper = entityStoreInfo.getMapper();
        Map<String, Method> selectMethodMap = entityStoreInfo.getSelectMethodMap();
        Method selectMethod = selectMethodMap.get(method);
        Map<String, Object> attachments = context.getAttachments();

        List<Object> ids = ReflectUtil.invoke(mapper, selectMethod, attachments, query);
        if (!ids.isEmpty()) {
            example.in(primaryKey, ids);
        } else {
            queryContext.setAbandoned(true);
        }
    }

}
