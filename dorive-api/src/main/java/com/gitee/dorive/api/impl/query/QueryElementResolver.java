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

package com.gitee.dorive.api.impl.query;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import com.gitee.dorive.api.entity.query.FieldDefinition;
import com.gitee.dorive.api.entity.query.QueryDefinition;
import com.gitee.dorive.api.entity.query.QueryFieldDefinition;
import com.gitee.dorive.api.entity.query.ele.FieldElement;
import com.gitee.dorive.api.entity.query.ele.QueryElement;
import com.gitee.dorive.api.entity.query.ele.QueryFieldElement;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
public class QueryElementResolver {

    public QueryElement resolve(QueryDefinition queryDefinition) {
        Class<?> queryType = ClassUtil.loadClass(queryDefinition.getGenericTypeName());

        QueryElement queryElement = new QueryElement();
        queryElement.setQueryDefinition(queryDefinition);
        queryElement.setGenericType(queryType);
        queryElement.setQueryFieldElements(resolveQueryFields(queryType, queryDefinition.getQueryFieldDefinitions()));
        queryElement.setSortByField(resolveField(queryType, queryDefinition.getSortByField()));
        queryElement.setOrderField(resolveField(queryType, queryDefinition.getOrderField()));
        queryElement.setPageField(resolveField(queryType, queryDefinition.getPageField()));
        queryElement.setLimitField(resolveField(queryType, queryDefinition.getLimitField()));
        return queryElement;
    }

    private FieldElement resolveField(Class<?> queryType, FieldDefinition fieldDefinition) {
        Field field = ReflectUtil.getField(queryType, fieldDefinition.getFieldName());

        Class<?> genericType = field.getType();
        if (Collection.class.isAssignableFrom(field.getType())) {
            ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
            Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
            genericType = (Class<?>) actualTypeArgument;
        }

        FieldElement fieldElement = new FieldElement();
        fieldElement.setFieldDefinition(fieldDefinition);
        fieldElement.setField(field);
        fieldElement.setType(field.getType());
        fieldElement.setGenericType(genericType);
        return fieldElement;
    }

    private List<QueryFieldElement> resolveQueryFields(Class<?> queryType, List<QueryFieldDefinition> queryFieldDefinitions) {
        List<QueryFieldElement> queryFieldElements = new ArrayList<>();
        for (QueryFieldDefinition queryFieldDefinition : queryFieldDefinitions) {
            FieldElement fieldElement = resolveField(queryType, queryFieldDefinition);
            QueryFieldElement queryFieldElement = BeanUtil.copyProperties(fieldElement, QueryFieldElement.class);
            queryFieldElement.setQueryFieldDefinition(queryFieldDefinition);
            queryFieldElements.add(queryFieldElement);
        }
        return queryFieldElements;
    }

}
