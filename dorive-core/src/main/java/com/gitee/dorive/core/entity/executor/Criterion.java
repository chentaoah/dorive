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

import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.core.api.constant.Operator;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.*;

@Data
@AllArgsConstructor
public class Criterion {

    private static final SimpleDateFormat SQL_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private String property;
    private String operator;
    private Object value;

    public String getFinalProperty() {
        return StrUtil.toUnderlineCase(this.property);
    }

    public String getFinalOperator() {
        String operator = this.operator;
        if (this.value instanceof Collection) {
            if (Operator.EQ.equals(operator)) {
                operator = Operator.IN;

            } else if (Operator.NE.equals(operator)) {
                operator = Operator.NOT_IN;
            }
        }
        return operator;
    }

    public Object getFinalValue() {
        return convert(this.value);
    }

    private String convert(Object value) {
        if (value instanceof Collection) {
            Collection<?> collection = (Collection<?>) value;
            List<String> values = new ArrayList<>(collection.size());
            for (Object item : collection) {
                values.add(doConvert(item));
            }
            return "(" + StrUtil.join(", ", values) + ")";

        } else if (operator.endsWith(Operator.IN)) {
            return "(" + doConvert(value) + ")";
        }
        return doConvert(value);
    }

    private String doConvert(Object value) {
        if (value instanceof Number) {
            return String.valueOf(value);

        } else if (value instanceof String) {
            if (operator.endsWith(Operator.LIKE)) {
                String string = (String) value;
                if (!string.startsWith("%") && !string.endsWith("%")) {
                    value = "%" + string + "%";
                }
            }
            return "'" + value + "'";

        } else if (value instanceof Date) {
            return "'" + SQL_DATE_FORMAT.format((Date) value) + "'";

        } else if (value == null || operator.startsWith(Operator.IS)) {
            return "NULL";
        }
        return value.toString();
    }

    @Override
    public String toString() {
        return getFinalProperty() + " " + getFinalOperator() + " " + getFinalValue();
    }

}
