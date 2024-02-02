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
import com.gitee.dorive.core.api.context.Options;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public abstract class AbstractContext implements Context {

    protected Map<Class<?>, Object> options = new LinkedHashMap<>(4);
    protected Map<String, Object> attachments = new LinkedHashMap<>(8);

    public AbstractContext(Options options) {
        this.options.putAll(options.getOptions());
    }

    public AbstractContext(Context anotherContext) {
        this.options.putAll(anotherContext.getOptions());
        this.attachments.putAll(anotherContext.getAttachments());
    }

    @Override
    public void setOption(Class<?> type, Object value) {
        options.put(type, value);
    }

    @Override
    public Object getOption(Class<?> type) {
        return options.get(type);
    }

    @Override
    public void removeOption(Class<?> type) {
        options.remove(type);
    }

    @Override
    public void setAttachment(String name, Object value) {
        attachments.put(name, value);
    }

    @Override
    public Object getAttachment(String name) {
        return attachments.get(name);
    }

    @Override
    public void removeAttachment(String name) {
        attachments.remove(name);
    }

}
