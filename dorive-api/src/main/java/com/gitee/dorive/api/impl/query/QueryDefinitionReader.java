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
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import com.gitee.dorive.api.annotation.query.Query;
import com.gitee.dorive.api.annotation.query.QueryField;
import com.gitee.dorive.api.entity.query.FieldDefinition;
import com.gitee.dorive.api.entity.query.QueryDefinition;
import com.gitee.dorive.api.entity.query.QueryFieldDefinition;
import com.gitee.dorive.api.util.ReflectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class QueryDefinitionReader {

    public QueryDefinition read(Class<?> entityType, Class<?> queryType) {
        QueryDefinition queryDefinition = new QueryDefinition();
        queryDefinition.setEntityTypeName(entityType.getName());
        queryDefinition.setGenericTypeName(queryType.getName());
        readFields(queryType, queryDefinition);
        return queryDefinition;
    }

    private void readFields(Class<?> queryType, QueryDefinition queryDefinition) {
        Query query = AnnotatedElementUtils.getMergedAnnotation(queryType, Query.class);
        Assert.notNull(query, "The @Query does not exist!");
        assert query != null;
        String[] ignoreFields = query.ignoreFields();
        String sortByField = query.sortByField();
        String orderField = query.orderField();
        String pageField = query.pageField();
        String limitField = query.limitField();

        List<QueryFieldDefinition> queryFieldDefinitions = new ArrayList<>();
        List<FieldDefinition> ignoreFieldDefinitions = new ArrayList<>();

        List<Field> fields = ReflectUtils.getAllFields(queryType);
        // 去重
        Map<String, Field> fieldMap = new LinkedHashMap<>();
        for (Field field : fields) {
            fieldMap.put(field.getName(), field);
        }
        for (Field field : fieldMap.values()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                String fieldName = field.getName();
                if (ArrayUtil.contains(ignoreFields, fieldName)) {
                    ignoreFieldDefinitions.add(readField(field));

                } else if (sortByField.equals(fieldName)) {
                    queryDefinition.setSortByField(readField(field));

                } else if (orderField.equals(fieldName)) {
                    queryDefinition.setOrderField(readField(field));

                } else if (pageField.equals(fieldName)) {
                    queryDefinition.setPageField(readField(field));

                } else if (limitField.equals(fieldName)) {
                    queryDefinition.setLimitField(readField(field));

                } else {
                    queryFieldDefinitions.add(readQueryField(field));
                }
            }
        }

        queryDefinition.setQueryFieldDefinitions(queryFieldDefinitions);
        queryDefinition.setIgnoreFieldDefinitions(ignoreFieldDefinitions);
    }

    private FieldDefinition readField(Field field) {
        Class<?> type = field.getType();
        boolean collection = false;
        Class<?> genericType = field.getType();
        String fieldName = field.getName();
        if (Collection.class.isAssignableFrom(type)) {
            collection = true;
            ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
            Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
            genericType = (Class<?>) actualTypeArgument;
        }
        FieldDefinition fieldDefinition = new FieldDefinition();
        fieldDefinition.setTypeName(type.getName());
        fieldDefinition.setCollection(collection);
        fieldDefinition.setGenericTypeName(genericType.getName());
        fieldDefinition.setFieldName(fieldName);
        return fieldDefinition;
    }

    private QueryFieldDefinition readQueryField(Field field) {
        QueryField queryField = AnnotatedElementUtils.getMergedAnnotation(field, QueryField.class);
        QueryFieldDefinition queryFieldDefinition = BeanUtil.copyProperties(readField(field), QueryFieldDefinition.class);
        if (queryField != null) {
            queryFieldDefinition.setBelongTo(queryField.belongTo());
            queryFieldDefinition.setField(StringUtils.isNotBlank(queryField.field()) ? queryField.field() : field.getName());
            queryFieldDefinition.setOperator(queryField.operator());
        } else {
            queryFieldDefinition.setBelongTo("/");
            queryFieldDefinition.setField(field.getName());
            queryFieldDefinition.setOperator("=");
        }
        return queryFieldDefinition;
    }

}
