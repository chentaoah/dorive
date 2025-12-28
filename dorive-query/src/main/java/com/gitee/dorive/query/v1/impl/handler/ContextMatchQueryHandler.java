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

import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import com.gitee.dorive.query.v1.api.QueryHandler;
import com.gitee.dorive.query.v1.entity.QueryContext;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ContextMatchQueryHandler implements QueryHandler {

    private final RepositoryContext repository;
    private final QueryHandler queryHandler;

    @Override
    public void handle(QueryContext queryContext, Object query) {
        RepositoryItem repositoryItem = repository.getRootRepository();
        if (!repository.matches(queryContext.getContext(), repositoryItem)) {
            queryContext.setAbandoned(true);
            return;
        }
        if (queryHandler != null) {
            queryHandler.handle(queryContext, query);
        }
    }

}
