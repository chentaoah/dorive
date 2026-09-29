/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.executor.impl.util;

import com.gitee.dorive.base.v1.executor.entity.qry.*;

import java.util.ArrayList;
import java.util.List;

public class ExampleUtils {

    public static Example clone(Example example) {
        if (example == null) {
            return null;
        }
        Example newExample = new InnerExample();

        List<String> selectProps = example.getSelectProps();
        if (selectProps != null) {
            newExample.select(new ArrayList<>(selectProps));
        }

        String selectSuffix = example.getSelectSuffix();
        if (selectSuffix != null) {
            newExample.setSelectSuffix(selectSuffix);
        }

        List<Criterion> criteria = example.getCriteria();
        if (criteria != null && !criteria.isEmpty()) {
            List<Criterion> newCriteria = newExample.getCriteria();
            for (Criterion criterion : criteria) {
                newCriteria.add(clone(criterion));
            }
        }

        OrderBy orderBy = example.getOrderBy();
        if (orderBy != null) {
            newExample.setOrderBy(clone(orderBy));
        }

        Page<Object> page = example.getPage();
        if (page != null) {
            newExample.setPage(clone(page));
        }

        return newExample;
    }

    public static Criterion clone(Criterion criterion) {
        return new Criterion(criterion.getProperty(), criterion.getOperator(), criterion.getValue());
    }

    public static OrderBy clone(OrderBy orderBy) {
        return new OrderBy(new ArrayList<>(orderBy.getProperties()), orderBy.getSort());
    }

    public static <T> Page<T> clone(Page<T> page) {
        return new Page<>(page.getTotal(), page.getCurrent(), page.getSize(), new ArrayList<>(page.getRecords()));
    }

}
