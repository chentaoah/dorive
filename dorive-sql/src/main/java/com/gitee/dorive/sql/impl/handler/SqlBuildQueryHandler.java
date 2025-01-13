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

import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.InnerExample;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.query.entity.QueryContext;
import com.gitee.dorive.query.entity.QueryUnit;
import com.gitee.dorive.query.impl.handler.AbstractQueryUnitQueryHandler;
import com.gitee.dorive.query.repository.AbstractQueryRepository;
import com.gitee.dorive.sql.impl.segment.SegmentResolver;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class SqlBuildQueryHandler extends AbstractQueryUnitQueryHandler {

    private final AbstractQueryRepository<?, ?> repository;

    @Override
    protected QueryUnit processQueryUnit(QueryContext queryContext, Map<String, QueryUnit> queryUnitMap, QueryUnit queryUnit) {
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
    protected void doHandle(QueryContext queryContext, Object query) {
        // ignore
    }

    @SuppressWarnings("unchecked")
    protected void doQuery(QueryContext queryContext, List<Object> ids) {
        Context context = queryContext.getContext();
        String primaryKey = queryContext.getPrimaryKey();
        Example example = queryContext.getExample();

        List<Object> entities = (List<Object>) getRepository().selectByExample(context, new InnerExample().in(primaryKey, ids));

        Page<Object> page = example.getPage();
        if (page != null) {
            page.setRecords(entities);
            queryContext.setResult(new Result<>(page));
        } else {
            queryContext.setResult(new Result<>(entities));
        }
    }
}
