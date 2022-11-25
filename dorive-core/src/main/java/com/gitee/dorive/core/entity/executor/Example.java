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
package com.gitee.dorive.core.entity.executor;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.core.api.constant.Operator;
import com.gitee.dorive.core.api.constant.Order;
import com.gitee.dorive.core.util.StringUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Example {

    private boolean emptyQuery = false;
    private String[] selectColumns;
    private List<Criterion> criteria = new ArrayList<>(4);
    private OrderBy orderBy;
    private Page<Object> page;

    public boolean isDirtyQuery() {
        return !criteria.isEmpty();
    }

    public boolean isQueryAll() {
        return !emptyQuery && !isDirtyQuery();
    }

    public void selectColumns(String... columns) {
        selectColumns = selectColumns == null ? columns : ArrayUtil.addAll(selectColumns, columns);
    }

    public void addCriterion(Criterion criterion) {
        criteria.add(criterion);
    }

    public String buildCriteria() {
        return StrUtil.join(" AND ", criteria);
    }

    public Example eq(String property, Object value) {
        criteria.add(new Criterion(property, Operator.EQ, value));
        return this;
    }

    public Example ne(String property, Object value) {
        criteria.add(new Criterion(property, Operator.NE, value));
        return this;
    }

    public Example in(String property, Object value) {
        criteria.add(new Criterion(property, Operator.IN, value));
        return this;
    }

    public Example notIn(String property, Object value) {
        criteria.add(new Criterion(property, Operator.NOT_IN, value));
        return this;
    }

    public Example isNull(String property) {
        criteria.add(new Criterion(property, Operator.IS, null));
        return this;
    }

    public Example isNotNull(String property) {
        criteria.add(new Criterion(property, Operator.IS_NOT, null));
        return this;
    }

    public Example like(String property, Object value) {
        criteria.add(new Criterion(property, Operator.LIKE, value));
        return this;
    }

    public Example notLike(String property, Object value) {
        criteria.add(new Criterion(property, Operator.NOT_LIKE, value));
        return this;
    }

    public Example gt(String property, Object value) {
        criteria.add(new Criterion(property, Operator.GT, value));
        return this;
    }

    public Example ge(String property, Object value) {
        criteria.add(new Criterion(property, Operator.GE, value));
        return this;
    }

    public Example lt(String property, Object value) {
        criteria.add(new Criterion(property, Operator.LT, value));
        return this;
    }

    public Example le(String property, Object value) {
        criteria.add(new Criterion(property, Operator.LE, value));
        return this;
    }

    public Example orderByAsc(String... columns) {
        orderBy = new OrderBy(StringUtils.toUnderlineCase(columns), Order.ASC);
        return this;
    }

    public Example orderByDesc(String... columns) {
        orderBy = new OrderBy(StringUtils.toUnderlineCase(columns), Order.DESC);
        return this;
    }

    public Example startPage(long pageNum, long pageSize) {
        page = new Page<>(pageNum, pageSize);
        return this;
    }

    public Example startPage() {
        page = new Page<>();
        return this;
    }

}
