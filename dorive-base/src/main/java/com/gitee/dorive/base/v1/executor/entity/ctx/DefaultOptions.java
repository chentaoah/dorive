/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.executor.entity.ctx;

import com.gitee.dorive.base.v1.executor.api.Options;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class DefaultOptions implements Options {

    private Map<Class<?>, Object> map;

    public DefaultOptions() {
        this.map = new ConcurrentHashMap<>(4);
    }

    public DefaultOptions(Map<Class<?>, Object> map) {
        this.map = map;
    }

    public DefaultOptions(Options options) {
        this.map = new ConcurrentHashMap<>(options.getMap());
    }

    @Override
    public <T> void setOption(Class<T> type, T value) {
        map.put(type, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getOption(Class<T> type) {
        return (T) map.get(type);
    }

    @Override
    public <T> T getOption(Class<T> type1, Class<? extends T> type2) {
        T option = getOption(type1);
        if (option == null) {
            option = getOption(type2);
        }
        return option;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getOption(Class<T> type1, Class<? extends T> type2, Class<?>... types) {
        T option = getOption(type1, type2);
        if (option != null) {
            return option;
        }
        if (types != null && types.length > 0) {
            for (Class<?> anyType : types) {
                T anyOption = (T) getOption(anyType);
                if (anyOption != null) {
                    return anyOption;
                }
            }
        }
        return null;
    }

    @Override
    public <T> void setOptions(Class<T> type, List<T> value) {
        map.put(type, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> getOptions(Class<T> type) {
        return (List<T>) map.get(type);
    }

    @Override
    public void remove(Class<?> type) {
        map.remove(type);
    }

}
