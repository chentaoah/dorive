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

package com.gitee.dorive.executor.v1.impl.executor;

import com.gitee.dorive.base.v1.definition.entity.EntityElement;
import com.gitee.dorive.base.v1.executor.entity.qry.Example;
import com.gitee.dorive.base.v1.executor.entity.op.Result;
import com.gitee.dorive.base.v1.executor.entity.qry.UnionExample;
import com.gitee.dorive.base.v1.factory.api.example.ExampleSerializer;
import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.base.v1.executor.api.Executor;
import com.gitee.dorive.base.v1.factory.api.entity.EntityMapper;
import com.gitee.dorive.base.v1.executor.entity.op.Condition;
import com.gitee.dorive.base.v1.executor.entity.op.Operation;
import com.gitee.dorive.base.v1.executor.entity.cop.ConditionUpdate;
import com.gitee.dorive.base.v1.executor.entity.cop.Query;
import com.gitee.dorive.base.v1.executor.entity.eop.Update;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
public class ExampleExecutor extends AbstractProxyExecutor {

    private EntityElement entityElement;
    private EntityMapper entityMapper;
    private ExampleSerializer exampleSerializer;

    public ExampleExecutor(Executor executor, //
                           EntityElement entityElement, //
                           EntityMapper entityMapper, //
                           ExampleSerializer exampleSerializer) {
        super(executor);
        this.entityElement = entityElement;
        this.entityMapper = entityMapper;
        this.exampleSerializer = exampleSerializer;
    }

    @Override
    public Result<Object> executeQuery(Context context, Query query) {
        Example example = query.getExample();
        if (example != null) {
            exampleSerializer.serialize(context, example);
        }
        if (example instanceof UnionExample) {
            convertUnion(context, (UnionExample) example);
        }
        return super.executeQuery(context, query);
    }

    @Override
    public long executeCount(Context context, Query query) {
        Example example = query.getExample();
        if (example != null) {
            exampleSerializer.serialize(context, example);
        }
        return super.executeCount(context, query);
    }

    @Override
    public int execute(Context context, Operation operation) {
        if (operation instanceof Condition condition) {
            Example example = condition.getExample();
            if (example != null) {
                exampleSerializer.serialize(context, example);
            }
        }
        if (operation instanceof Update) {
            convertUpdate((Update) operation);
        }
        if (operation instanceof ConditionUpdate) {
            convertConditionUpdate((ConditionUpdate) operation);
        }
        return super.execute(context, operation);
    }

    private void convertUnion(Context context, UnionExample unionExample) {
        List<Example> examples = unionExample.getExamples();
        for (Example example : examples) {
            exampleSerializer.serialize(context, example.getCriteria());
        }
    }

    private void convertUpdate(Update update) {
        Set<String> nullableProps = update.getNullableProps();
        if (nullableProps != null && !nullableProps.isEmpty()) {
            nullableProps = entityMapper.serialize(nullableProps);
            update.setNullableProps(nullableProps);
        }
    }

    private void convertConditionUpdate(ConditionUpdate conditionUpdate) {
        Set<String> nullableProps = conditionUpdate.getNullableProps();
        if (nullableProps != null && !nullableProps.isEmpty()) {
            nullableProps = entityMapper.serialize(nullableProps);
            conditionUpdate.setNullableProps(nullableProps);
        }
    }

}
