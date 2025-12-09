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

package com.gitee.dorive.core.impl.factory;

import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.api.constant.core.Sort;
import com.gitee.dorive.api.entity.core.def.OrderByDef;
import com.gitee.dorive.core.entity.executor.OrderBy;
import com.gitee.dorive.core.impl.util.ExampleUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
public class OrderByFactory {

    private final OrderByDef orderByDef;
    private final OrderBy orderBy;

    public OrderByFactory(OrderByDef orderByDef) {
        this.orderByDef = orderByDef;
        this.orderBy = newOrderBy(orderByDef);
    }

    private OrderBy newOrderBy(OrderByDef orderByDef) {
        if (orderByDef != null) {
            String field = orderByDef.getField();
            String sort = orderByDef.getSort();
            if (StringUtils.isNotBlank(field) && StringUtils.isNotBlank(sort)) {
                sort = sort.toUpperCase();
                if (Sort.ASC.equals(sort) || Sort.DESC.equals(sort)) {
                    List<String> properties = StrUtil.splitTrim(field, ",");
                    return new OrderBy(properties, sort);
                }
            }
        }
        return null;
    }

    public OrderBy newOrderBy() {
        return orderBy != null ? ExampleUtils.clone(orderBy) : null;
    }

}
