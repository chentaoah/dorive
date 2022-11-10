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
package com.gitee.spring.domain.core.entity.executor;

import cn.hutool.core.util.StrUtil;
import com.gitee.spring.domain.core.api.Operator;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
public class Criterion {

    private static final SimpleDateFormat SQL_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private String property;
    private String operator;
    private Object value;

    @Override
    public String toString() {
        String property = StrUtil.toUnderlineCase(this.property);
        String value = convert(operator, this.value);
        return property + " " + operator + " " + value;
    }

    private String convert(String operator, Object value) {
        if (value instanceof Collection) {
            Collection<?> collection = (Collection<?>) value;
            List<String> values = new ArrayList<>(collection.size());
            for (Object item : collection) {
                values.add(doConvert(operator, item));
            }
            return " (" + StrUtil.join(",", values) + ") ";

        } else if (operator.endsWith(Operator.IN)) {
            return " (" + doConvert(operator, value) + ") ";
        }
        return doConvert(operator, value);
    }

    private String doConvert(String operator, Object value) {
        if (value instanceof Number) {
            return String.valueOf(value);

        } else if (value instanceof String) {
            return "'" + value + "'";

        } else if (value instanceof Date) {
            return "'" + SQL_DATE_FORMAT.format((Date) value) + "'";

        } else if (value == null || operator.startsWith(Operator.IS)) {
            return "NULL";
        }
        return value.toString();
    }

}
