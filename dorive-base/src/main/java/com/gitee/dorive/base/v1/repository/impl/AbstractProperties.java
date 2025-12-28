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

package com.gitee.dorive.base.v1.repository.impl;

import cn.hutool.core.lang.Assert;
import com.gitee.dorive.base.v1.repository.api.Properties;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractProperties implements Properties {
    protected Map<Class<?>, Object> properties = new ConcurrentHashMap<>(8);

    @Override
    public <T> void setProperty(Class<T> type, T instance) {
        properties.put(type, instance);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getProperty(Class<T> type) {
        Object value = properties.get(type);
        Assert.notNull(value, "The property cannot be null!");
        return (T) value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T tryGetProperty(Class<T> type) {
        return (T) properties.get(type);
    }
}
