/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.repository.v1.impl.repository.ele;

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
