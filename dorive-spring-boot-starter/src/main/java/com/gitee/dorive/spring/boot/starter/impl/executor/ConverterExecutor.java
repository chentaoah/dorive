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
import com.gitee.dorive.core.api.executor.Converter;
import com.gitee.dorive.core.api.executor.Executor;
import com.gitee.dorive.core.entity.executor.Criterion;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.entity.executor.UnionExample;
import com.gitee.dorive.core.entity.operation.Condition;
import com.gitee.dorive.core.entity.operation.Operation;
import com.gitee.dorive.core.entity.operation.Query;
import com.gitee.dorive.core.impl.executor.AbstractProxyExecutor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public class ConverterExecutor extends AbstractProxyExecutor {

    private EntityEle entityEle;
    private Map<String, Converter> converterMap;

    public ConverterExecutor(Executor executor, EntityEle entityEle, Map<String, Converter> converterMap) {
        super(executor);
        this.entityEle = entityEle;
        this.converterMap = converterMap;
    }

    @Override
    public Result<Object> executeQuery(Context context, Query query) {
        Example example = query.getExample();
        if (example != null) {
            if (example instanceof UnionExample) {
                convert(context, (UnionExample) example);
            } else {
                convert(context, example);
            }
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
        return super.execute(context, operation);
    }

    public void convert(Context context, UnionExample unionExample) {
        List<Example> examples = unionExample.getExamples();
        for (Example example : examples) {
            convert(context, example);
        }
    }

    public void convert(Context context, Example example) {
        List<Criterion> criteria = example.getCriteria();
        if (criteria != null && !criteria.isEmpty()) {
            for (Criterion criterion : criteria) {
                String property = criterion.getProperty();
                Object value = criterion.getValue();
                Converter converter = converterMap.get(property);
                if (converter != null) {
                    value = converter.convert(context, criterion, value);
                    criterion.setValue(value);
                }
            }
        }
    }

}
