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

package com.gitee.dorive.spring.boot.starter.impl;

import com.baomidou.mybatisplus.core.conditions.interfaces.Compare;
import com.gitee.dorive.spring.boot.starter.api.CriterionAppender;
import com.gitee.dorive.api.constant.Operator;
import com.gitee.dorive.spring.boot.starter.util.SqlUtils;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AppenderContext {

    public final static Map<String, CriterionAppender> OPERATOR_CRITERION_APPENDER_MAP = new ConcurrentHashMap<>();

    static {
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.EQ, (abstractWrapper, property, value) -> {
            if (value instanceof Collection) {
                abstractWrapper.in(property, (Collection<?>) value);
            } else {
                abstractWrapper.eq(property, value);
            }
        });
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.NE, (abstractWrapper, property, value) -> {
            if (value instanceof Collection) {
                abstractWrapper.notIn(property, (Collection<?>) value);
            } else {
                abstractWrapper.ne(property, value);
            }
        });
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.GT, Compare::gt);
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.GE, Compare::ge);
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.LT, Compare::lt);
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.LE, Compare::le);
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.IN, (abstractWrapper, property, value) -> abstractWrapper.in(property, (Collection<?>) value));
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.NOT_IN, (abstractWrapper, property, value) -> abstractWrapper.notIn(property, (Collection<?>) value));
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.LIKE, (abstractWrapper, property, value) -> abstractWrapper.like(property, SqlUtils.toLike(value)));
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.NOT_LIKE, (abstractWrapper, property, value) -> abstractWrapper.notLike(property, SqlUtils.toLike(value)));
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.IS_NULL, (abstractWrapper, property, value) -> abstractWrapper.isNull(property));
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.IS_NOT_NULL, (abstractWrapper, property, value) -> abstractWrapper.isNotNull(property));
        OPERATOR_CRITERION_APPENDER_MAP.put(Operator.NULL_SWITCH, (abstractWrapper, property, value) -> {
            if (value instanceof Boolean) {
                if ((Boolean) value) {
                    abstractWrapper.isNull(property);
                } else {
                    abstractWrapper.isNotNull(property);
                }
            }
        });
    }

}
