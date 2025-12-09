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

package com.gitee.dorive.core.impl.util;

import com.gitee.dorive.core.entity.executor.*;

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
