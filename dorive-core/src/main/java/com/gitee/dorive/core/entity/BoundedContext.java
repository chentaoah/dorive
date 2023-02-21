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
import com.gitee.dorive.core.api.Task;
import com.gitee.dorive.core.impl.selector.ChainSelector;
import com.gitee.dorive.core.impl.selector.NameSelector;
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

    private boolean relay = false;
    private boolean observer = false;
    private Selector selector = NameSelector.EMPTY_SELECTOR;
    private Map<String, Task> tasks = Collections.emptyMap();
    private Map<String, Object> attachments = Collections.emptyMap();

    @Deprecated
    public BoundedContext() {
        selector = new SceneSelector();
    }

    @Deprecated
    public BoundedContext(String... scenes) {
        this.selector = new SceneSelector(scenes);
    }

    public BoundedContext(boolean observer) {
        this.observer = observer;
    }

    public BoundedContext(Selector selector) {
        this.selector = selector;
    }

    public BoundedContext(boolean relay, Selector selector) {
        this.relay = relay;
        this.selector = selector;
    }

    public boolean isMatch(CommonRepository repository) {
        return selector.isMatch(this, repository);
    }

    public boolean isRelay(CommonRepository repository) {
        return selector.isRelay(this, repository);
    }

    public List<String> selectColumns(CommonRepository repository) {
        return selector.selectColumns(this, repository);
    }

    public void appendNames(String... namesToAdd) {
        if (namesToAdd != null && namesToAdd.length > 0) {
            selector = new ChainSelector(selector, namesToAdd);
        }
    }

    public void putTask(String name, Task task) {
        if (tasks == Collections.EMPTY_MAP) {
            tasks = new ConcurrentHashMap<>();
        }
        tasks.put(name, task);
    }

    public Ref ref(Object object) {
        return new Ref(this, object);
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
