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
import com.gitee.dorive.api.constant.Operator;
import com.gitee.dorive.core.util.CriterionUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = false)
public class MultiInBuilder {

    private List<String> aliases;
    private int number;
    private int size;
    private List<Object> values;

    public MultiInBuilder(List<String> aliases, int number) {
        this.aliases = aliases;
        this.number = number;
        this.size = aliases.size();
        this.values = new ArrayList<>(number * size);
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public void append(Object value) {
        values.add(value);
    }

    public void clear() {
        int size = values.size();
        int remainder = size % this.size;
        values.subList(size - remainder, size).clear();
    }

    public Criterion toCriterion() {
        String property = StrUtil.join(",", aliases);
        StringBuilder builder = new StringBuilder();
        int page = values.size() / size;
        for (int current = 1; current <= page; current++) {
            List<Object> subValues = values.subList((current - 1) * size, current * size);
            builder.append(buildValuesStr(subValues));
        }
        String valuesStr = StrUtil.removeSuffix(builder, ",");
        return new Criterion(property, Operator.MULTI_IN, valuesStr);
    }

    public String buildValuesStr(List<Object> values) {
        return values.stream().map(CriterionUtils::sqlParam).collect(Collectors.joining(",", "(", "),"));
    }

}
