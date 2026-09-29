/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.executor.impl.factory;

import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.base.v1.definition.constant.Sort;
import com.gitee.dorive.base.v1.definition.def.OrderByDef;
import com.gitee.dorive.base.v1.executor.entity.qry.OrderBy;
import com.gitee.dorive.base.v1.executor.impl.util.ExampleUtils;
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
