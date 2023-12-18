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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class MultiInBuilder {

    private List<String> aliases;
    private int recordSize;
    private List<MultiValue> multiValues;
    private int valueSize;

    public MultiInBuilder(int recordSize, List<String> aliases) {
        this.aliases = aliases;
        this.recordSize = recordSize;
        this.multiValues = new ArrayList<>(recordSize);
        this.valueSize = aliases.size();
    }

    public boolean isEmpty() {
        return multiValues.isEmpty();
    }

    public void append(Object value) {
        if (multiValues.isEmpty()) {
            multiValues.add(new MultiValue(0, new Object[valueSize]));
        }
        MultiValue lastMultiValue = multiValues.get(multiValues.size() - 1);
        int index = lastMultiValue.getIndex();
        Object[] values = lastMultiValue.getValues();
        if (index >= values.length) {
            multiValues.add(new MultiValue(0, new Object[valueSize]));
            lastMultiValue = multiValues.get(multiValues.size() - 1);
        }
        doAppend(lastMultiValue, value);
    }

    private void doAppend(MultiValue lastMultiValue, Object value) {
        int index = lastMultiValue.getIndex();
        Object[] values = lastMultiValue.getValues();
        values[index] = value;
        lastMultiValue.setIndex(index + 1);
    }

    public void clear() {
        multiValues.remove(multiValues.size() - 1);
    }

    public Criterion build() {
        String aliasesStr = StrUtil.join(",", aliases);
        return new Criterion(aliasesStr, Operator.MULTI_IN, this);
    }

    @Data
    @AllArgsConstructor
    public static class MultiValue {
        private int index;
        private Object[] values;
    }

}
