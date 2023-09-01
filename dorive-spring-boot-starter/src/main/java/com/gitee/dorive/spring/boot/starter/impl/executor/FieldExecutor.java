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
import com.gitee.dorive.core.entity.executor.OrderBy;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public class FieldExecutor extends AbstractExampleExecutor {

    private EntityEle entityEle;
    private Map<String, Converter> converterMap;

    public FieldExecutor(Executor executor, EntityEle entityEle, Map<String, Converter> converterMap) {
        super(executor);
        this.entityEle = entityEle;
        this.converterMap = converterMap;
    }

    @Override
    public void convert(Context context, Example example) {
        convertSelectProps(example);
        convertCriteria(context, example.getCriteria());
        convertOrderBy(example.getOrderBy());
    }

    public void convertSelectProps(Example example) {
        List<String> properties = example.getSelectProps();
        if (properties != null && !properties.isEmpty()) {
            properties = entityEle.toAliases(properties);
            example.setSelectProps(properties);
        }
    }

    public void convertCriteria(Context context, List<Criterion> criteria) {
        if (criteria != null && !criteria.isEmpty()) {
            for (Criterion criterion : criteria) {
                String property = criterion.getProperty();
                String alias = entityEle.toAlias(property);
                criterion.setProperty(alias);

                Object value = criterion.getValue();
                if (converterMap != null && !converterMap.isEmpty()) {
                    Converter converter = converterMap.get(property);
                    if (converter != null) {
                        Object mappedValue = converter.convert(context, criterion, value);
                        criterion.setValue(mappedValue);
                    }
                }
            }
        }
    }

    public void convertOrderBy(OrderBy orderBy) {
        if (orderBy != null) {
            List<String> properties = orderBy.getProperties();
            properties = entityEle.toAliases(properties);
            orderBy.setProperties(properties);
        }
    }

}
