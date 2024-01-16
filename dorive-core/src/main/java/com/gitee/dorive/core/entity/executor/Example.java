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

import com.gitee.dorive.api.constant.Operator;
import com.gitee.dorive.api.constant.Order;
import com.gitee.dorive.core.util.StringUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@Data
@NoArgsConstructor
public class Example {

    private List<String> selectProps;
    private List<String> extraProps;
    private List<Criterion> criteria = new ArrayList<>(4);
    private OrderBy orderBy;
    private Page<Object> page;

    public Example(List<Criterion> criteria) {
        this.criteria = criteria;
    }

    public void select(List<String> fields) {
        selectProps = fields;
    }

    public void select(String... fields) {
        select(StringUtils.toList(fields));
    }

    public void selectExtra(List<String> fields) {
        if (extraProps == null) {
            extraProps = fields;
        } else {
            extraProps.addAll(fields);
        }
    }

    public void selectExtra(String... fields) {
        selectExtra(StringUtils.toList(fields));
    }

    public boolean isEmpty() {
        return criteria.isEmpty();
    }

    public boolean isNotEmpty() {
        return !criteria.isEmpty();
    }

    public Example eq(String field, Object value) {
        criteria.add(new Criterion(field, Operator.EQ, value));
        return this;
    }

    public Example ne(String field, Object value) {
        criteria.add(new Criterion(field, Operator.NE, value));
        return this;
    }

    public Example gt(String field, Object value) {
        criteria.add(new Criterion(field, Operator.GT, value));
        return this;
    }

    public Example ge(String field, Object value) {
        criteria.add(new Criterion(field, Operator.GE, value));
        return this;
    }

    public Example lt(String field, Object value) {
        criteria.add(new Criterion(field, Operator.LT, value));
        return this;
    }

    public Example le(String field, Object value) {
        criteria.add(new Criterion(field, Operator.LE, value));
        return this;
    }

    public Example in(String field, Object value) {
        criteria.add(new Criterion(field, Operator.IN, value));
        return this;
    }

    public Example notIn(String field, Object value) {
        criteria.add(new Criterion(field, Operator.NOT_IN, value));
        return this;
    }

    public Example like(String field, Object value) {
        criteria.add(new Criterion(field, Operator.LIKE, value));
        return this;
    }

    public Example notLike(String field, Object value) {
        criteria.add(new Criterion(field, Operator.NOT_LIKE, value));
        return this;
    }

    public Example isNull(String field) {
        criteria.add(new Criterion(field, Operator.IS_NULL));
        return this;
    }

    public Example isNotNull(String field) {
        criteria.add(new Criterion(field, Operator.IS_NOT_NULL));
        return this;
    }

    public Example and(Consumer<Example> consumer) {
        Example example = new InnerExample();
        consumer.accept(example);
        criteria.add(new Criterion("@Lambda", Operator.AND, example));
        return this;
    }

    public Example or(Consumer<Example> consumer) {
        Example example = new InnerExample();
        consumer.accept(example);
        criteria.add(new Criterion("@Lambda", Operator.OR, example));
        return this;
    }

    public Example orderByAsc(String... fields) {
        orderBy = new OrderBy(Arrays.asList(fields), Order.ASC);
        return this;
    }

    public Example orderByDesc(String... fields) {
        orderBy = new OrderBy(Arrays.asList(fields), Order.DESC);
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
