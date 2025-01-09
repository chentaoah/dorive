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

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import com.gitee.dorive.api.entity.query.QueryDefinition;
import com.gitee.dorive.api.entity.query.QueryFieldDefinition;
import com.gitee.dorive.api.entity.query.def.QueryDef;
import com.gitee.dorive.api.entity.query.def.QueryFieldDef;
import com.gitee.dorive.api.util.ReflectUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class QueryDefinitionResolver {

    public QueryDefinition resolve(Class<?> queryType) {
        QueryDef queryDef = QueryDef.fromElement(queryType);
        Assert.notNull(queryDef, "The @Query does not exist!");
        assert queryDef != null;

        QueryDefinition queryDefinition = new QueryDefinition();
        queryDefinition.setQueryDef(queryDef);
        queryDefinition.setGenericType(queryType);
        readFields(queryType, queryDef, queryDefinition);
        return queryDefinition;
    }

    private void readFields(Class<?> queryType, QueryDef queryDef, QueryDefinition queryDefinition) {
        String[] ignoreFieldNames = queryDef.getIgnoreFields();
        String sortByField = queryDef.getSortByField();
        String orderField = queryDef.getOrderField();
        String pageField = queryDef.getPageField();
        String limitField = queryDef.getLimitField();

        List<QueryFieldDefinition> queryFieldDefinitions = new ArrayList<>();
        List<com.gitee.dorive.api.entity.core.Field> ignoreFields = new ArrayList<>();

        List<Field> fields = ReflectUtils.getAllFields(queryType);
        // 去重
        Map<String, Field> fieldMap = new LinkedHashMap<>();
        for (Field field : fields) {
            fieldMap.put(field.getName(), field);
        }
        for (Field field : fieldMap.values()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                String fieldName = field.getName();
                if (ArrayUtil.contains(ignoreFieldNames, fieldName)) {
                    ignoreFields.add(readField(field));

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
        queryDefinition.setIgnoreFields(ignoreFields);
    }

    private com.gitee.dorive.api.entity.core.Field readField(Field field) {
        return new com.gitee.dorive.api.entity.core.Field(field);
    }

    private QueryFieldDefinition readQueryField(Field field) {
        QueryFieldDefinition queryFieldDefinition = new QueryFieldDefinition(field);
        QueryFieldDef queryFieldDef = QueryFieldDef.fromElement(field);
        if (queryFieldDef != null) {
            String fieldName = queryFieldDef.getField();
            queryFieldDef.setField(StringUtils.isNotBlank(fieldName) ? fieldName : field.getName());
        } else {
            queryFieldDef = new QueryFieldDef();
            queryFieldDef.setBelongTo("/");
            queryFieldDef.setField(field.getName());
            queryFieldDef.setOperator("=");
        }
        queryFieldDefinition.setQueryFieldDef(queryFieldDef);
        return queryFieldDefinition;
    }

}
