/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.gitee.spring.domain.coating.entity;

import cn.hutool.core.convert.Convert;
import com.gitee.spring.domain.core.api.constant.Order;
import com.gitee.spring.domain.core.entity.executor.OrderBy;
import com.gitee.spring.domain.core.entity.executor.Page;
import com.gitee.spring.domain.core.util.StringUtils;
import lombok.Data;

@Data
public class SpecificProperties {

    private PropertyWrapper orderByAscProperty;
    private PropertyWrapper orderByDescProperty;
    private PropertyWrapper pageNumProperty;
    private PropertyWrapper pageSizeProperty;

    public boolean addProperty(String fieldName, PropertyWrapper propertyWrapper) {
        if ("orderByAsc".equals(fieldName)) {
            orderByAscProperty = propertyWrapper;
            return true;

        } else if ("orderByDesc".equals(fieldName)) {
            orderByDescProperty = propertyWrapper;
            return true;

        } else if ("pageNum".equals(fieldName)) {
            pageNumProperty = propertyWrapper;
            return true;

        } else if ("pageSize".equals(fieldName)) {
            pageSizeProperty = propertyWrapper;
            return true;
        }
        return false;
    }

    public OrderBy getOrderBy(Object coatingObject) {
        if (orderByAscProperty != null) {
            Object orderByAsc = orderByAscProperty.getProperty().getFieldValue(coatingObject);
            if (orderByAsc != null) {
                String[] columns = StringUtils.toStringArray(orderByAsc);
                if (columns != null && columns.length > 0) {
                    return new OrderBy(columns, Order.ASC);
                }
            }
        }
        if (orderByDescProperty != null) {
            Object orderByDesc = orderByDescProperty.getProperty().getFieldValue(coatingObject);
            if (orderByDesc != null) {
                String[] columns = StringUtils.toStringArray(orderByDesc);
                if (columns != null && columns.length > 0) {
                    return new OrderBy(columns, Order.DESC);
                }
            }
        }
        return null;
    }

    public Page<Object> getPage(Object coatingObject) {
        if (pageNumProperty != null && pageSizeProperty != null) {
            Object pageNum = pageNumProperty.getProperty().getFieldValue(coatingObject);
            Object pageSize = pageSizeProperty.getProperty().getFieldValue(coatingObject);
            if (pageNum != null && pageSize != null) {
                return new Page<>(Convert.convert(Long.class, pageNum), Convert.convert(Long.class, pageSize));
            }
        }
        return null;
    }

}
