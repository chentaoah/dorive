/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.executor.impl.util;

import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.base.v1.definition.constant.Operator;
import com.gitee.dorive.base.v1.executor.entity.qry.Criterion;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = false)
public class MultiInBuilder {

    private List<String> properties;
    private int size;
    private int count;
    private List<Object> values;

    public MultiInBuilder(List<String> properties, int count) {
        this.properties = properties;
        this.size = properties.size();
        this.count = count;
        this.values = new ArrayList<>(count * size);
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public void append(Object value) {
        values.add(value);
    }

    public void clearRemainder() {
        int total = values.size();
        int remainder = total % size;
        if (remainder != 0) {
            values.subList(total - remainder, total).clear();
        }
    }

    public void clearLast() {
        int total = values.size();
        int remainder = total % size;
        if (remainder == 0) {
            values.subList(total - size, total).clear();
        }
    }

    public Criterion toCriterion() {
        String propertiesStr = StrUtil.join(",", properties);
        return new Criterion(propertiesStr, Operator.MULTI_IN, this);
    }

    public String buildPropertiesStr() {
        return StrUtil.join(",", properties);
    }

    public String buildValuesStr() {
        StringBuilder builder = new StringBuilder();
        int page = values.size() / size;
        for (int current = 1; current <= page; current++) {
            List<Object> subValues = values.subList((current - 1) * size, current * size);
            builder.append(buildValuesStr(subValues));
        }
        return StrUtil.removeSuffix(builder, ",");
    }

    private String buildValuesStr(List<Object> values) {
        return values.stream()
                .map(value -> CriterionUtils.doGetValue(null, value))
                .collect(Collectors.joining(",", "(", "),"));
    }

}
