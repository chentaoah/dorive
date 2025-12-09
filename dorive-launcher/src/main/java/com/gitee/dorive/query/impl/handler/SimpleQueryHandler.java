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

package com.gitee.dorive.query.impl.handler;

import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.query.api.QueryHandler;
import com.gitee.dorive.query.entity.QueryContext;
import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
public class SimpleQueryHandler implements QueryHandler {

    private final QueryHandler queryHandler;

    @Override
    public void handle(QueryContext queryContext, Object query) {
        Map<String, Example> exampleMap = queryContext.getExampleMap();
        if (exampleMap.size() == 1 && exampleMap.containsKey("/")) {
            return;
        }
        if (queryHandler != null) {
            queryHandler.handle(queryContext, query);
        }
    }

}
