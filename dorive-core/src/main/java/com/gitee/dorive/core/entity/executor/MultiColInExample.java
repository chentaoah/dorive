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

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class MultiColInExample extends Example {

    private int size;
    private List<String> properties;
    private int step;
    private List<Object> values;
    private int cursor;

    public MultiColInExample(int size, List<String> properties) {
        this.size = size;
        this.properties = properties;
        this.step = properties.size();
        this.values = new ArrayList<>(size * step);
        this.cursor = 0;
    }

    public void add(Object value) {
        if (values.size() - cursor >= step) {
            cursor += step;
        }
        values.add(value);
    }

    public void clear() {
        values.subList(cursor, values.size()).clear();
    }

    public int page() {
        return values.size() / step;
    }

    public List<Object> get(int page) {
        int fromIndex = (page - 1) * step;
        int toIndex = fromIndex + step;
        return values.subList(fromIndex, toIndex);
    }

}
