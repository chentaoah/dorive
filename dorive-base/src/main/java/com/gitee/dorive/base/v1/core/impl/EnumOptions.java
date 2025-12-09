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

package com.gitee.dorive.base.v1.core.impl;

import com.gitee.dorive.base.v1.core.api.Options;

import java.util.Collections;
import java.util.Map;

public class EnumOptions implements Options {

    private final Map<Class<?>, Object> options;

    public EnumOptions(Class<?> type, Object value) {
        this.options = Collections.singletonMap(type, value);
    }

    @Override
    public Map<Class<?>, Object> getOptions() {
        return options;
    }

    @Override
    public <T> void setOption(Class<T> type, T value) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getOption(Class<T> type) {
        return (T) options.get(type);
    }

    @Override
    public void removeOption(Class<?> type) {
        throw new UnsupportedOperationException();
    }

}
