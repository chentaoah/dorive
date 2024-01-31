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
import com.gitee.dorive.core.api.context.Selector;
import com.gitee.dorive.core.entity.option.Selection;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class StaticContext implements Context {

    private Map<Class<?>, Object> options = new LinkedHashMap<>(3);

    public StaticContext(Selection selection) {
        this.options.put(Selection.class, selection);
        this.options = Collections.unmodifiableMap(this.options);
    }

    public StaticContext(Selection selection, Selector selector) {
        this.options.put(Selection.class, selection);
        this.options.put(Selector.class, selector);
        this.options = Collections.unmodifiableMap(this.options);
    }

    @Override
    public Map<Class<?>, Object> getOptions() {
        return options;
    }

    @Override
    public Map<String, Object> getAttachments() {
        return Collections.emptyMap();
    }

}