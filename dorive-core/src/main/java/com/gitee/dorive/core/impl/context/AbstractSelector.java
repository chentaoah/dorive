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

package com.gitee.dorive.core.impl.context;

import com.gitee.dorive.core.api.context.Selector;
import com.gitee.dorive.core.entity.option.SelectType;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractSelector implements Selector {

    private final Map<Class<?>, Object> options = new LinkedHashMap<>(3);

    public AbstractSelector() {
        this.options.put(SelectType.class, SelectType.SELECTOR);
        this.options.put(Selector.class, this);
    }

    @Override
    public Map<Class<?>, Object> getOptions() {
        return options;
    }

    @Override
    public void setOption(Class<?> type, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getOption(Class<?> type) {
        return options.get(type);
    }

    @Override
    public void removeOption(Class<?> type) {
        throw new UnsupportedOperationException();
    }

}
