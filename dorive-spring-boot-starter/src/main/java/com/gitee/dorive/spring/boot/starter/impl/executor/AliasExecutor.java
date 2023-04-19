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

package com.gitee.dorive.spring.boot.starter.impl.executor;

import com.gitee.dorive.api.entity.element.EntityEle;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.executor.Executor;
import com.gitee.dorive.core.entity.executor.Criterion;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.OrderBy;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.entity.executor.UnionExample;
import com.gitee.dorive.core.entity.operation.Condition;
import com.gitee.dorive.core.entity.operation.Operation;
import com.gitee.dorive.core.entity.operation.Query;
import com.gitee.dorive.core.impl.executor.AbstractExecutor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AliasExecutor extends AbstractExecutor {

    private EntityEle entityEle;
    private Executor executor;

    @Override
    public Result<Object> executeQuery(Context context, Query query) {
        Example example = query.getExample();
        if (example != null) {
            if (example instanceof UnionExample) {
                convert((UnionExample) example);
            } else {
                convert(example);
            }
        }
        return executor.executeQuery(context, query);
    }

    @Override
    public long executeCountQuery(Context context, Query query) {
        Example example = query.getExample();
        if (example != null) {
            convert(example);
        }
        return executor.executeCountQuery(context, query);
    }

    @Override
    public int execute(Context context, Operation operation) {
        if (operation instanceof Condition) {
            Condition condition = (Condition) operation;
            Example example = condition.getExample();
            if (example != null) {
                convert(example);
            }
        }
        return executor.execute(context, operation);
    }

    public void convert(UnionExample unionExample) {
        List<Example> examples = unionExample.getExamples();
        for (Example example : examples) {
            convert(example);
        }
    }

    public void convert(Example example) {
        List<String> selectColumns = example.getSelectColumns();
        if (selectColumns != null && !selectColumns.isEmpty()) {
            selectColumns = entityEle.toAliases(selectColumns);
            example.selectColumns(selectColumns);
        }

        List<Criterion> criteria = example.getCriteria();
        if (criteria != null && !criteria.isEmpty()) {
            for (Criterion criterion : criteria) {
                String property = criterion.getProperty();
                property = entityEle.toAlias(property);
                criterion.setProperty(property);
            }
        }

        OrderBy orderBy = example.getOrderBy();
        if (orderBy != null) {
            List<String> orderByColumns = orderBy.getColumns();
            orderByColumns = entityEle.toAliases(orderByColumns);
            orderBy.setColumns(orderByColumns);
        }
    }

}
