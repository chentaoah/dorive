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

package com.gitee.dorive.mybatis_plus.impl;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.gitee.dorive.api.constant.core.Operator;
import com.gitee.dorive.core.entity.executor.Criterion;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.OrderBy;
import com.gitee.dorive.mybatis_plus.api.CriterionAppender;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AppenderContext {

    public final static Map<String, CriterionAppender> OPERATOR_CRITERION_APPENDER_MAP = new ConcurrentHashMap<>();

    static {
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.EQ, (wrapper, example, property, value) -> {
            if (value instanceof Collection) {
                wrapper.in(property, (Collection<?>) value);
            } else {
                wrapper.eq(property, value);
            }
        });
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.NE, (wrapper, example, property, value) -> {
            if (value instanceof Collection) {
                wrapper.notIn(property, (Collection<?>) value);
            } else {
                wrapper.ne(property, value);
            }
        });
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.GT, (wrapper, example, property, value) -> wrapper.gt(property, value));
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.GE, (wrapper, example, property, value) -> wrapper.ge(property, value));
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.LT, (wrapper, example, property, value) -> wrapper.lt(property, value));
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.LE, (wrapper, example, property, value) -> wrapper.le(property, value));
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.IN, (wrapper, example, property, value) -> wrapper.in(property, (Collection<?>) value));
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.NOT_IN, (wrapper, example, property, value) -> wrapper.notIn(property, (Collection<?>) value));
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.LIKE, (wrapper, example, property, value) -> wrapper.like(property, value));
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.NOT_LIKE, (wrapper, example, property, value) -> wrapper.notLike(property, value));
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.IS_NULL, (wrapper, example, property, value) -> wrapper.isNull(property));
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.IS_NOT_NULL, (wrapper, example, property, value) -> wrapper.isNotNull(property));
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.MULTI_IN, (wrapper, example, property, value) -> {
            List<Criterion> criteria = example.getCriteria();
            OrderBy orderBy = example.getOrderBy();
            String prefix = criteria.size() == 1 ? " WHERE " : " AND ";
            String lastSql = prefix + "(" + property + ") IN (" + value + ")";
            if (orderBy != null) {
                lastSql = lastSql + StringPool.SPACE + orderBy;
                example.setOrderBy(null);
            }
            wrapper.last(lastSql);
        });
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.MULTI_NOT_IN, (wrapper, example, property, value) -> {
            List<Criterion> criteria = example.getCriteria();
            OrderBy orderBy = example.getOrderBy();
            String prefix = criteria.size() == 1 ? " WHERE " : " AND ";
            String lastSql = prefix + "(" + property + ") NOT IN (" + value + ")";
            if (orderBy != null) {
                lastSql = lastSql + StringPool.SPACE + orderBy;
                example.setOrderBy(null);
            }
            wrapper.last(lastSql);
        });
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.AND, (wrapper, example, property, value) -> {
            if (value instanceof Example) {
                wrapper.and(q -> appendCriterion(q, (Example) value));
            }
        });
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.OR, (wrapper, example, property, value) -> {
            if (value instanceof Example) {
                wrapper.or(q -> appendCriterion(q, (Example) value));
            }
        });
    }

    public static void appendCriterion(AbstractWrapper<?, String, ?> wrapper, Example example) {
        List<Criterion> criteria = example.getCriteria();
        for (Criterion criterion : criteria) {
            CriterionAppender criterionAppender = OPERATOR_CRITERION_APPENDER_MAP.get(criterion.getOperator());
            criterionAppender.appendCriterion(wrapper, example, criterion.getProperty(), criterion.getValue());
        }
    }

}
