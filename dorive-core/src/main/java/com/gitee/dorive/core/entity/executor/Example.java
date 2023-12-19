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

    public void select(List<String> properties) {
        selectProps = properties;
    }

    public void select(String... properties) {
        select(StringUtils.toList(properties));
    }

    public void selectExtra(List<String> properties) {
        if (extraProps == null) {
            extraProps = properties;
        } else {
            extraProps.addAll(properties);
        }
    }

    public void selectExtra(String... properties) {
        selectExtra(StringUtils.toList(properties));
    }

    public boolean isEmpty() {
        return criteria.isEmpty();
    }

    public boolean isNotEmpty() {
        return !criteria.isEmpty();
    }

    public Example eq(String property, Object value) {
        criteria.add(new Criterion(property, Operator.EQ, value));
        return this;
    }

    public Example ne(String property, Object value) {
        criteria.add(new Criterion(property, Operator.NE, value));
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

    public Example in(String property, Object value) {
        criteria.add(new Criterion(property, Operator.IN, value));
        return this;
    }

    public Example notIn(String property, Object value) {
        criteria.add(new Criterion(property, Operator.NOT_IN, value));
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

    public Example isNull(String property) {
        criteria.add(new Criterion(property, Operator.IS_NULL));
        return this;
    }

    public Example isNotNull(String property) {
        criteria.add(new Criterion(property, Operator.IS_NOT_NULL));
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

    public Example orderByAsc(String... properties) {
        orderBy = new OrderBy(Arrays.asList(properties), Order.ASC);
        return this;
    }

    public Example orderByDesc(String... properties) {
        orderBy = new OrderBy(Arrays.asList(properties), Order.DESC);
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
