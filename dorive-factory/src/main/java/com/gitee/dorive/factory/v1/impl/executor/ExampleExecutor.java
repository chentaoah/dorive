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

package com.gitee.dorive.factory.v1.impl.executor;

import com.gitee.dorive.base.v1.common.constant.Operator;
import com.gitee.dorive.base.v1.common.entity.EntityElement;
import com.gitee.dorive.base.v1.core.entity.qry.Criterion;
import com.gitee.dorive.base.v1.core.entity.qry.Example;
import com.gitee.dorive.base.v1.core.entity.qry.OrderBy;
import com.gitee.dorive.base.v1.core.entity.op.Result;
import com.gitee.dorive.base.v1.core.entity.qry.UnionExample;
import com.gitee.dorive.factory.v1.api.ExampleConverter;
import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.base.v1.executor.api.Executor;
import com.gitee.dorive.base.v1.executor.impl.AbstractProxyExecutor;
import com.gitee.dorive.factory.v1.api.EntityMapper;
import com.gitee.dorive.factory.v1.api.FieldMapper;
import com.gitee.dorive.base.v1.core.entity.op.Condition;
import com.gitee.dorive.base.v1.core.entity.op.Operation;
import com.gitee.dorive.base.v1.core.entity.cop.ConditionUpdate;
import com.gitee.dorive.base.v1.core.entity.cop.Query;
import com.gitee.dorive.base.v1.core.entity.eop.Update;
import com.gitee.dorive.base.v1.executor.util.MultiInBuilder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
public class ExampleExecutor extends AbstractProxyExecutor implements ExampleConverter {

    private EntityElement entityElement;
    private EntityMapper entityMapper;

    public ExampleExecutor(Executor executor, EntityElement entityElement, EntityMapper entityMapper) {
        super(executor);
        this.entityElement = entityElement;
        this.entityMapper = entityMapper;
    }

    @Override
    public Result<Object> executeQuery(Context context, Query query) {
        Example example = query.getExample();
        if (example != null) {
            convert(context, example);
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
            convert(context, example);
        }
        return super.executeCount(context, query);
    }

    @Override
    public int execute(Context context, Operation operation) {
        if (operation instanceof Condition) {
            Condition condition = (Condition) operation;
            Example example = condition.getExample();
            if (example != null) {
                convert(context, example);
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

    @Override
    public void convert(Context context, Example example) {
        convertSelectProps(example);
        convertCriteria(context, example);
        convertOrderBy(example);
    }

    private void convertUnion(Context context, UnionExample unionExample) {
        List<Example> examples = unionExample.getExamples();
        for (Example example : examples) {
            convertCriteria(context, example);
        }
    }

    private void convertUpdate(Update update) {
        Set<String> nullableProps = update.getNullableProps();
        if (nullableProps != null && !nullableProps.isEmpty()) {
            nullableProps = entityMapper.toAliases(nullableProps);
            update.setNullableProps(nullableProps);
        }
    }

    private void convertConditionUpdate(ConditionUpdate conditionUpdate) {
        Set<String> nullableProps = conditionUpdate.getNullableProps();
        if (nullableProps != null && !nullableProps.isEmpty()) {
            nullableProps = entityMapper.toAliases(nullableProps);
            conditionUpdate.setNullableProps(nullableProps);
        }
    }

    private void convertSelectProps(Example example) {
        List<String> properties = example.getSelectProps();
        if (properties != null && !properties.isEmpty()) {
            properties = entityMapper.toAliases(properties);
            example.setSelectProps(properties);
        }
    }

    private void convertCriteria(Context context, Example example) {
        List<Criterion> criteria = example.getCriteria();
        if (criteria != null && !criteria.isEmpty()) {
            for (Criterion criterion : criteria) {
                String operator = criterion.getOperator();
                if (Operator.AND.equals(operator) || Operator.OR.equals(operator)) {
                    Object value = criterion.getValue();
                    if (value instanceof Example) {
                        convert(context, (Example) value);
                    }
                } else if (Operator.MULTI_IN.equals(operator)) {
                    Object value = criterion.getValue();
                    if (value instanceof MultiInBuilder) {
                        MultiInBuilder builder = (MultiInBuilder) value;
                        List<String> properties = builder.getProperties();
                        properties = entityMapper.toAliases(properties);
                        builder.setProperties(properties);
                        criterion.setProperty(builder.buildPropertiesStr());
                        criterion.setValue(builder.buildValuesStr());
                    }
                } else {
                    doConvertCriteria(criterion);
                }
            }
        }
    }

    private void doConvertCriteria(Criterion criterion) {
        String property = criterion.getProperty();
        Object value = criterion.getValue();

        FieldMapper fieldMapper = entityMapper.getFieldMapperByField(property);
        if (fieldMapper == null) {
            fieldMapper = entityMapper.getFieldMapperByAlias(property);
        }
        if (fieldMapper != null) {
            property = fieldMapper.getAlias();
            value = fieldMapper.deconstruct(value);
            criterion.setProperty(property);
            criterion.setValue(value);
        }
    }

    private void convertOrderBy(Example example) {
        OrderBy orderBy = example.getOrderBy();
        if (orderBy != null) {
            List<String> properties = orderBy.getProperties();
            properties = entityMapper.toAliases(properties);
            orderBy.setProperties(properties);
        }
    }

}
