/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.executor.api;

import com.gitee.dorive.base.v1.executor.entity.ctx.DefaultOptions;
import com.gitee.dorive.base.v1.executor.impl.matcher.AllMatcher;
import com.gitee.dorive.base.v1.executor.impl.matcher.NoneMatcher;
import com.gitee.dorive.base.v1.executor.impl.matcher.RootMatcher;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface Options {

    Options NONE = new DefaultOptions(Collections.singletonMap(Matcher.class, new NoneMatcher()));
    Options ROOT = new DefaultOptions(Collections.singletonMap(Matcher.class, new RootMatcher()));
    Options ALL = new DefaultOptions(Collections.singletonMap(Matcher.class, new AllMatcher()));

    Map<Class<?>, Object> getMap();

    <T> void setOption(Class<T> type, T value);

    <T> T getOption(Class<T> type);

    <T> T getOption(Class<T> type1, Class<? extends T> type2);

    <T> T getOption(Class<T> type1, Class<? extends T> type2, Class<?>... types);

    <T> void setOptions(Class<T> type, List<T> value);

    <T> List<T> getOptions(Class<T> type);

    void remove(Class<?> type);

}
