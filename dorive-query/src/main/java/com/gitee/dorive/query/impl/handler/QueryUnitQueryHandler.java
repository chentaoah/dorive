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
import com.gitee.dorive.core.entity.executor.InnerExample;
import com.gitee.dorive.query.api.QueryHandler;
import com.gitee.dorive.query.api.QueryUnitHandler;
import com.gitee.dorive.query.entity.MergedRepository;
import com.gitee.dorive.query.entity.QueryContext;
import com.gitee.dorive.query.entity.QueryUnit;
import lombok.AllArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class QueryUnitQueryHandler implements QueryHandler {

    private final QueryHandler queryHandler;

    @Override
    public void handle(QueryContext queryContext, Object query) {
        if (queryHandler instanceof QueryUnitHandler) {
            Map<String, QueryUnit> queryUnitMap = newQueryUnitMap(queryContext, (QueryUnitHandler) queryHandler);
            queryContext.setQueryUnitMap(queryUnitMap);
            queryContext.setQueryUnit(queryUnitMap.get("/"));
        }
        if (queryHandler != null) {
            queryHandler.handle(queryContext, query);
        }
    }

    private Map<String, QueryUnit> newQueryUnitMap(QueryContext queryContext, QueryUnitHandler queryUnitHandler) {
        Map<String, Example> exampleMap = queryContext.getExampleMap();
        List<MergedRepository> mergedRepositories = queryUnitHandler.getMergedRepositories(queryContext);
        Map<String, QueryUnit> queryUnitMap = new LinkedHashMap<>(mergedRepositories.size() * 4 / 3 + 1);
        for (MergedRepository mergedRepository : mergedRepositories) {
            String absoluteAccessPath = mergedRepository.getAbsoluteAccessPath();
            Example example = exampleMap.computeIfAbsent(absoluteAccessPath, key -> new InnerExample());
            QueryUnit queryUnit = new QueryUnit(mergedRepository, example, false, null);
            queryUnit = queryUnitHandler.processQueryUnit(queryContext, queryUnitMap, queryUnit);
            queryUnitMap.put(absoluteAccessPath, queryUnit);
        }
        return queryUnitMap;
    }

}
