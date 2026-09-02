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

package com.gitee.dorive.base.v1.executor.api;

import com.gitee.dorive.base.v1.executor.entity.ctx.DefaultOptions;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.gitee.dorive.base.v1.executor.impl.selector.DefaultSelector.*;

public interface Options {

    Options NONE = new DefaultOptions(Collections.singletonMap(Selector.class, NONE_SELECTOR));
    Options ROOT = new DefaultOptions(Collections.singletonMap(Selector.class, ROOT_SELECTOR));
    Options ALL = new DefaultOptions(Collections.singletonMap(Selector.class, ALL_SELECTOR));

    Map<Class<?>, Object> getMap();

    <T> void setOption(Class<T> type, T value);

    <T> T getOption(Class<T> type);

    <T> void setOptions(Class<T> type, List<T> value);

    <T> List<T> getOptions(Class<T> type);

    void remove(Class<?> type);

}
