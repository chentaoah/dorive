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

package com.gitee.dorive.query.v1.impl.handler;

import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.query.v1.api.QueryHandler;
import com.gitee.dorive.query.v1.entity.QueryContext;
import com.gitee.dorive.query.v1.enums.QueryMode;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class AdaptiveQueryHandler implements QueryHandler {

    private final Map<QueryMode, QueryHandler> queryHandlerMap;

    public AdaptiveQueryHandler(Map<QueryMode, QueryHandler> queryHandlerMap) {
        this.queryHandlerMap = queryHandlerMap;
    }

    @Override
    public void handle(QueryContext queryContext, Object query) {
        Context context = queryContext.getContext();
        QueryMode queryMode = context.getOption(QueryMode.class);
        // 如果开发者没有指定
        if (queryMode == null) {
            String method = queryContext.getMethod();
            if (StringUtils.isNotBlank(method)) {
                queryMode = QueryMode.SQL_CUSTOM;
            } else {
                queryMode = QueryMode.SQL_EXECUTE;
            }
        }
        QueryHandler queryHandler = queryHandlerMap.get(queryMode);
        if (queryHandler != null) {
            queryHandler.handle(queryContext, query);
        }
    }

}
