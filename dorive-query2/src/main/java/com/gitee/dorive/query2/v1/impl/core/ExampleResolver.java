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

package com.gitee.dorive.query2.v1.impl.core;

import cn.hutool.core.convert.Convert;
import com.gitee.dorive.base.v1.common.constant.Sort;
import com.gitee.dorive.base.v1.common.entity.Field;
import com.gitee.dorive.base.v1.common.entity.QueryDefinition;
import com.gitee.dorive.base.v1.core.entity.qry.OrderBy;
import com.gitee.dorive.base.v1.core.entity.qry.Page;
import com.gitee.dorive.base.v1.core.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ExampleResolver {

    private QueryDefinition queryDefinition;

    public OrderBy newOrderBy(Object query) {
        Field sortByField = queryDefinition.getSortByField();
        Field orderField = queryDefinition.getOrderField();
        if (sortByField != null && orderField != null) {
            Object sortBy = sortByField.getFieldValue(query);
            Object order = orderField.getFieldValue(query);
            if (sortBy != null && order instanceof String) {
                List<String> properties = StringUtils.toList(sortBy);
                if (properties != null && !properties.isEmpty()) {
                    String orderStr = ((String) order).toUpperCase();
                    if (Sort.ASC.equals(orderStr) || Sort.DESC.equals(orderStr)) {
                        return new OrderBy(properties, orderStr);
                    }
                }
            }
        }
        return null;
    }

    public Page<Object> newPage(Object query) {
        Field pageField = queryDefinition.getPageField();
        Field limitField = queryDefinition.getLimitField();
        if (pageField != null && limitField != null) {
            Object page = pageField.getFieldValue(query);
            Object limit = limitField.getFieldValue(query);
            if (page != null && limit != null) {
                return new Page<>(Convert.convert(Long.class, page), Convert.convert(Long.class, limit));
            }
        }
        return null;
    }
}
