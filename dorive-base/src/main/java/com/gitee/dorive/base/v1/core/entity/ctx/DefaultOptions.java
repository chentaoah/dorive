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

package com.gitee.dorive.base.v1.core.entity.ctx;

import com.gitee.dorive.base.v1.core.api.Options;
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
