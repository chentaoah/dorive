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

package com.gitee.dorive.mybatis.v1.impl.handler;

import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.base.v1.core.entity.op.Result;
import com.gitee.dorive.base.v1.core.entity.qry.Example;
import com.gitee.dorive.base.v1.core.entity.qry.InnerExample;
import com.gitee.dorive.base.v1.core.entity.qry.Page;
import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import com.gitee.dorive.base.v1.repository.impl.AbstractRepository;
import com.gitee.dorive.mybatis.v1.impl.segment.SegmentResolver;
import com.gitee.dorive.query.v1.entity.QueryContext;
import com.gitee.dorive.query.v1.entity.QueryUnit;
import com.gitee.dorive.query.v1.impl.handler.executor.AbstractQueryUnitQueryHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class SqlBuildQueryHandler extends AbstractQueryUnitQueryHandler {

    private final RepositoryContext repository;

    @Override
    public QueryUnit processQueryUnit(QueryContext queryContext, Map<String, QueryUnit> queryUnitMap, QueryUnit queryUnit) {
        processExample(queryContext, queryUnit);
        processAttachment(queryContext, queryUnitMap, queryUnit);
        return queryUnit;
    }

    protected void processExample(QueryContext queryContext, QueryUnit queryUnit) {
        queryUnit.convertExample(queryContext);
    }

    protected void processAttachment(QueryContext queryContext, Map<String, QueryUnit> queryUnitMap, QueryUnit queryUnit) {
        SegmentResolver segmentResolver = new SegmentResolver(repository, queryContext, queryUnitMap, queryUnit);
        queryUnit.setAttachment(segmentResolver.resolve());
    }

    @Override
    public void doHandle(QueryContext queryContext, Object query) {
        // ignore
    }

    @SuppressWarnings("unchecked")
    protected void doQuery(QueryContext queryContext, List<Object> ids) {
        Context context = queryContext.getContext();
        String primaryKey = queryContext.getPrimaryKey();
        Example example = queryContext.getExample();

        List<Object> entities = Collections.emptyList();
        if (repository instanceof AbstractRepository) {
            Example newExample = new InnerExample().in(primaryKey, ids);
            newExample.setOrderBy(example.getOrderBy());
            entities = ((AbstractRepository<Object, Object>) repository).selectByExample(context, newExample);
        }

        Page<Object> page = example.getPage();
        if (page != null) {
            page.setRecords(entities);
            queryContext.setResult(new Result<>(page));
        } else {
            queryContext.setResult(new Result<>(entities));
        }
    }
}
