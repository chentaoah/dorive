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

package com.gitee.dorive.coating.entity;

import cn.hutool.core.convert.Convert;
import com.gitee.dorive.api.constant.Order;
import com.gitee.dorive.core.entity.executor.OrderBy;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.core.util.StringUtils;
import lombok.Data;

import java.util.List;

@Data
public class SpecificFields {

    private CoatingField sortByField;
    private CoatingField orderField;
    private CoatingField pageField;
    private CoatingField limitField;

    public boolean addProperty(CoatingField field) {
        String fieldName = field.getName();
        if ("sortBy".equals(fieldName)) {
            sortByField = field;
            return true;

        } else if ("order".equals(fieldName)) {
            orderField = field;
            return true;

        } else if ("page".equals(fieldName)) {
            pageField = field;
            return true;

        } else if ("limit".equals(fieldName)) {
            limitField = field;
            return true;
        }
        return false;
    }

    public OrderBy newOrderBy(Object coating) {
        if (sortByField != null && orderField != null) {
            Object sortBy = sortByField.getFieldValue(coating);
            Object order = orderField.getFieldValue(coating);
            if (sortBy != null && order instanceof String) {
                List<String> properties = StringUtils.toList(sortBy);
                if (properties != null && !properties.isEmpty()) {
                    String orderStr = ((String) order).toUpperCase();
                    if (Order.ASC.equals(orderStr) || Order.DESC.equals(orderStr)) {
                        return new OrderBy(properties, orderStr);
                    }
                }
            }
        }
        return null;
    }

    public Page<Object> newPage(Object coating) {
        if (pageField != null && limitField != null) {
            Object page = pageField.getFieldValue(coating);
            Object limit = limitField.getFieldValue(coating);
            if (page != null && limit != null) {
                return new Page<>(Convert.convert(Long.class, page), Convert.convert(Long.class, limit));
            }
        }
        return null;
    }

}
