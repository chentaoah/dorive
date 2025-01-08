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
import com.gitee.dorive.query.api.QueryHandler;
import com.gitee.dorive.query.entity.QueryContext;
import com.gitee.dorive.query.entity.QueryUnit;
import com.gitee.dorive.query.impl.handler.QueryUnitQueryHandler;
import com.gitee.dorive.query.repository.AbstractQueryRepository;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class SqlCustomQueryHandler extends QueryUnitQueryHandler {

    private final EntityStoreInfo entityStoreInfo;

    public SqlCustomQueryHandler(AbstractQueryRepository<?, ?> repository, QueryHandler queryHandler, EntityStoreInfo entityStoreInfo) {
        super(repository, queryHandler);
        this.entityStoreInfo = entityStoreInfo;
    }

    @Override
    public void handle(QueryContext queryContext, Object query) {
        super.handle(queryContext, query);
        Object mapper = entityStoreInfo.getMapper();
        Method selectByQueryMethod = entityStoreInfo.getSelectByQueryMethod();
        Context context = queryContext.getContext();
        Map<String, Object> attachments = context.getAttachments();
        List<Object> ids = ReflectUtil.invoke(mapper, selectByQueryMethod, attachments, query);
        if (!ids.isEmpty()) {
            Example example = queryContext.getExample();
            QueryUnit queryUnit = queryContext.getQueryUnit();
            String primaryKey = queryUnit.getPrimaryKey();
            example.in(primaryKey, ids);
        } else {
            queryContext.setAbandoned(true);
        }
    }

}
