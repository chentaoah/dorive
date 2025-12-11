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

package com.gitee.dorive.query.v1.impl.handler.executor;

import com.gitee.dorive.base.v1.core.entity.qry.Example;
import com.gitee.dorive.base.v1.core.entity.qry.InnerExample;
import com.gitee.dorive.query.v1.api.QueryHandler;
import com.gitee.dorive.query.v1.entity.MergedRepository;
import com.gitee.dorive.query.v1.entity.QueryContext;
import com.gitee.dorive.query.v1.entity.QueryUnit;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractQueryUnitQueryHandler implements QueryHandler {

    @Override
    public void handle(QueryContext queryContext, Object query) {
        Map<String, QueryUnit> queryUnitMap = newQueryUnitMap(queryContext);
        queryContext.setQueryUnitMap(queryUnitMap);
        queryContext.setQueryUnit(queryUnitMap.get("/"));
        doHandle(queryContext, query);
    }

    protected Map<String, QueryUnit> newQueryUnitMap(QueryContext queryContext) {
        Map<String, Example> exampleMap = queryContext.getExampleMap();
        List<MergedRepository> mergedRepositories = getMergedRepositories(queryContext);
        Map<String, QueryUnit> queryUnitMap = new LinkedHashMap<>(mergedRepositories.size() * 4 / 3 + 1);
        for (MergedRepository mergedRepository : mergedRepositories) {
            String absoluteAccessPath = mergedRepository.getAbsoluteAccessPath();
            Example example = exampleMap.computeIfAbsent(absoluteAccessPath, key -> new InnerExample());
            QueryUnit queryUnit = new QueryUnit(mergedRepository, example, false, null);
            queryUnit = processQueryUnit(queryContext, queryUnitMap, queryUnit);
            queryUnitMap.put(absoluteAccessPath, queryUnit);
        }
        return queryUnitMap;
    }

    protected List<MergedRepository> getMergedRepositories(QueryContext queryContext) {
        return queryContext.getQueryConfig().getMergedRepositories();
    }

    protected QueryUnit processQueryUnit(QueryContext queryContext, Map<String, QueryUnit> queryUnitMap, QueryUnit queryUnit) {
        return queryUnit;
    }

    public abstract void doHandle(QueryContext queryContext, Object query);

}
