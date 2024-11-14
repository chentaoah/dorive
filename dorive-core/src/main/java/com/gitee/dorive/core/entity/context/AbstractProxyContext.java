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

package com.gitee.dorive.core.entity.context;

import com.gitee.dorive.core.api.context.Context;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public abstract class AbstractProxyContext implements Context {

    protected Context context;

    @Override
    public Map<Class<?>, Object> getOptions() {
        return context.getOptions();
    }

    @Override
    public <T> void setOption(Class<T> type, T value) {
        context.setOption(type, value);
    }

    @Override
    public <T> T getOption(Class<T> type) {
        return context.getOption(type);
    }

    @Override
    public void removeOption(Class<?> type) {
        context.removeOption(type);
    }

    @Override
    public Map<String, Object> getAttachments() {
        return context.getAttachments();
    }

    @Override
    public void setAttachment(String name, Object value) {
        context.setAttachment(name, value);
    }

    @Override
    public Object getAttachment(String name) {
        return context.getAttachment(name);
    }

    @Override
    public void removeAttachment(String name) {
        context.removeAttachment(name);
    }

}
