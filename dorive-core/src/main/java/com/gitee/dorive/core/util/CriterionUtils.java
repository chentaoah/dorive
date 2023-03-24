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

package com.gitee.dorive.core.util;

import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.api.constant.Operator;
import com.gitee.dorive.core.entity.executor.Criterion;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class CriterionUtils {

    private static final SimpleDateFormat SQL_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String getOperator(Criterion criterion) {
        String operator = criterion.getOperator();
        Object value = criterion.getValue();
        if (value instanceof Collection) {
            if (Operator.EQ.equals(operator)) {
                operator = Operator.IN;

            } else if (Operator.NE.equals(operator)) {
                operator = Operator.NOT_IN;
            }
        }
        return operator;
    }

    public static Object getValue(Criterion criterion) {
        return convertValue(criterion);
    }

    private static String convertValue(Criterion criterion) {
        String operator = criterion.getOperator();
        Object value = criterion.getValue();
        if (value instanceof Collection) {
            Collection<?> collection = (Collection<?>) value;
            List<String> values = new ArrayList<>(collection.size());
            for (Object item : collection) {
                values.add(doConvertValue(criterion, item));
            }
            return "(" + StrUtil.join(", ", values) + ")";

        } else if (operator.endsWith(Operator.IN)) {
            return "(" + doConvertValue(criterion, value) + ")";
        }
        return doConvertValue(criterion, value);
    }

    private static String doConvertValue(Criterion criterion, Object value) {
        String operator = criterion.getOperator();
        if (value instanceof Number) {
            return String.valueOf(value);

        } else if (value instanceof String) {
            if (operator.endsWith(Operator.LIKE)) {
                value = SqlUtils.toLike(value);
            }
            return "'" + value + "'";

        } else if (value instanceof Date) {
            return "'" + SQL_DATE_FORMAT.format((Date) value) + "'";

        } else if (value == null || operator.startsWith("IS")) {
            return "NULL";
        }
        return value.toString();
    }

    public static String toString(Criterion criterion) {
        StringBuilder builder = new StringBuilder();
        builder.append(criterion.getProperty()).append(" ").append(getOperator(criterion));
        String operator = criterion.getOperator();
        if (!operator.startsWith("IS")) {
            builder.append(" ").append(getValue(criterion));
        }
        return builder.toString();
    }

}
