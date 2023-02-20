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
package com.gitee.dorive.core.entity;

import com.gitee.dorive.core.api.ExampleBuilder;
import com.gitee.dorive.core.api.Selector;
import com.gitee.dorive.core.impl.selector.SceneSelector;
import com.gitee.dorive.core.repository.CommonRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@EqualsAndHashCode(callSuper = false)
public class BoundedContext {

    private boolean observer;
    private Selector selector;
    private Map<String, Object> attachments = Collections.emptyMap();

    public BoundedContext() {
        selector = new SceneSelector();
    }

    public BoundedContext(Selector selector) {
        this.selector = selector;
    }

    public BoundedContext(String... scenes) {
        this.selector = new SceneSelector(scenes);
    }

    public boolean isMatch(CommonRepository repository) {
        return selector.isMatch(this, repository);
    }

    public List<String> selectColumns(CommonRepository repository) {
        return selector.selectColumns(this, repository);
    }

    public Object put(String key, Object value) {
        if (attachments == Collections.EMPTY_MAP) {
            attachments = new ConcurrentHashMap<>();
        }
        return attachments.put(key, value);
    }

    public boolean containsKey(String key) {
        return attachments.containsKey(key);
    }

    public Object get(String key) {
        return attachments.get(key);
    }

    public Object remove(String key) {
        return attachments.remove(key);
    }

    public void putBuilder(String key, ExampleBuilder builder) {
        put(key, builder);
    }

}
