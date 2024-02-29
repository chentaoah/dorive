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

package com.gitee.dorive.mybatis.plus.impl;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.gitee.dorive.api.constant.Operator;
import com.gitee.dorive.core.entity.executor.Criterion;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.mybatis.plus.api.CriterionAppender;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AppenderContext {

    public final static Map<String, CriterionAppender> OPERATOR_CRITERION_APPENDER_MAP = new ConcurrentHashMap<>();

    static {
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.EQ, (abstractWrapper, criteriaNum, property, value) -> {
            if (value instanceof Collection) {
                abstractWrapper.in(property, (Collection<?>) value);
            } else {
                abstractWrapper.eq(property, value);
            }
        });
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.NE, (abstractWrapper, criteriaNum, property, value) -> {
            if (value instanceof Collection) {
                abstractWrapper.notIn(property, (Collection<?>) value);
            } else {
                abstractWrapper.ne(property, value);
            }
        });
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.GT, (abstractWrapper, criteriaNum, property, value) -> abstractWrapper.gt(property, value));
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.GE, (abstractWrapper, criteriaNum, property, value) -> abstractWrapper.ge(property, value));
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.LT, (abstractWrapper, criteriaNum, property, value) -> abstractWrapper.lt(property, value));
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.LE, (abstractWrapper, criteriaNum, property, value) -> abstractWrapper.le(property, value));
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.IN, (abstractWrapper, criteriaNum, property, value) -> abstractWrapper.in(property, (Collection<?>) value));
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.NOT_IN, (abstractWrapper, criteriaNum, property, value) -> abstractWrapper.notIn(property, (Collection<?>) value));
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.LIKE, (abstractWrapper, criteriaNum, property, value) -> abstractWrapper.like(property, value));
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.NOT_LIKE, (abstractWrapper, criteriaNum, property, value) -> abstractWrapper.notLike(property, value));
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.IS_NULL, (abstractWrapper, criteriaNum, property, value) -> abstractWrapper.isNull(property));
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.IS_NOT_NULL, (abstractWrapper, criteriaNum, property, value) -> abstractWrapper.isNotNull(property));
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.MULTI_IN, (abstractWrapper, criteriaNum, property, value) -> {
            String prefix = criteriaNum == 1 ? " WHERE " : " AND ";
            abstractWrapper.last(prefix + "(" + property + ") IN (" + value + ")");
        });
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.MULTI_NOT_IN, (abstractWrapper, criteriaNum, property, value) -> {
            String prefix = criteriaNum == 1 ? " WHERE " : " AND ";
            abstractWrapper.last(prefix + "(" + property + ") NOT IN (" + value + ")");
        });
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.AND, (abstractWrapper, criteriaNum, property, value) -> {
            if (value instanceof Example) {
                abstractWrapper.and(q -> appendCriterion(q, (Example) value));
            }
        });
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.OR, (abstractWrapper, criteriaNum, property, value) -> {
            if (value instanceof Example) {
                abstractWrapper.or(q -> appendCriterion(q, (Example) value));
            }
        });
    }

    public static void appendCriterion(AbstractWrapper<?, String, ?> abstractWrapper, Example example) {
        List<Criterion> criteria = example.getCriteria();
        int criteriaNum = criteria.size();
        for (Criterion criterion : criteria) {
            CriterionAppender criterionAppender = OPERATOR_CRITERION_APPENDER_MAP.get(criterion.getOperator());
            criterionAppender.appendCriterion(abstractWrapper, criteriaNum, criterion.getProperty(), criterion.getValue());
        }
    }

}
