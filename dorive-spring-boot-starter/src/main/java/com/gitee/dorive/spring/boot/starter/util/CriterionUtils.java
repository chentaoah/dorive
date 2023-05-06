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

package com.gitee.dorive.spring.boot.starter.util;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.gitee.dorive.api.constant.Operator;
import com.gitee.dorive.core.entity.executor.Criterion;

import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

public class CriterionUtils {

    public static String getOperator(Criterion criterion) {
        String operator = criterion.getOperator();
        Object value = criterion.getValue();
        if (value instanceof Collection) {
            if (Operator.EQ.equals(operator)) {
                operator = Operator.IN;

            } else if (Operator.NE.equals(operator)) {
                operator = Operator.NOT_IN;
            }
        } else {
            if (Operator.IN.equals(operator)) {
                operator = Operator.EQ;

            } else if (Operator.NOT_IN.equals(operator)) {
                operator = Operator.NE;

            } else if (Operator.NULL_SWITCH.equals(operator)) {
                if (value instanceof Boolean) {
                    return (Boolean) value ? Operator.IS_NULL : Operator.IS_NOT_NULL;
                }
            }
        }
        return operator;
    }

    public static Object getValue(Criterion criterion) {
        String operator = criterion.getOperator();
        Object value = criterion.getValue();
        if (value instanceof Collection) {
            Collection<?> collection = (Collection<?>) value;
            return collection.stream().map(item -> sqlParam(operator, item)).collect(Collectors.joining(", ", "(", ")"));
        }
        return sqlParam(operator, value);
    }

    public static String sqlParam(String operator, Object value) {
        if (value instanceof Date) {
            value = DateUtil.formatDateTime((Date) value);
        }
        if (Operator.LIKE.equals(operator) || Operator.NOT_LIKE.equals(operator)) {
            value = SqlUtils.toLike(value);
        }
        return StringUtils.quotaMark(value);
    }

    public static String toString(Criterion criterion) {
        String property = criterion.getProperty();
        String operator = getOperator(criterion);
        if (Operator.IS_NULL.equals(operator) || Operator.IS_NOT_NULL.equals(operator)) {
            return property + " " + operator;
        } else {
            return property + " " + operator + " " + getValue(criterion);
        }
    }

}
