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

package com.gitee.dorive.core.impl.context;

import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.context.Matcher;
import com.gitee.dorive.core.api.context.Selector;
import com.gitee.dorive.core.entity.option.SelectType;
import com.gitee.dorive.core.repository.CommonRepository;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 双向依赖，在获取hashCode时，会栈溢出
 */
@Getter
@Setter
public class SelectTypeMatcher implements Matcher {

    private CommonRepository repository;
    private Map<SelectType, Matcher> matcherMap = new LinkedHashMap<>(8);

    public SelectTypeMatcher(CommonRepository repository) {
        this.repository = repository;
        this.matcherMap.put(SelectType.NONE, context -> false);
        this.matcherMap.put(SelectType.ROOT, context -> repository.isRoot());
        this.matcherMap.put(SelectType.ALL, context -> true);
        this.matcherMap.put(SelectType.SELECTOR, new SelectorMatcher());
    }

    @Override
    public boolean matches(Context context) {
        Map<Class<?>, Object> options = context.getOptions();
        SelectType selectType = (SelectType) options.get(SelectType.class);
        if (selectType != null) {
            Matcher matcher = matcherMap.get(selectType);
            if (matcher != null) {
                return matcher.matches(context);
            }
        }
        return false;
    }

    private class SelectorMatcher implements Matcher {
        @Override
        public boolean matches(Context context) {
            Map<Class<?>, Object> options = context.getOptions();
            Selector selector = (Selector) options.get(Selector.class);
            if (selector != null) {
                Set<String> names = selector.getNames();
                String name = repository.getName();
                return names.contains(name);
            }
            return false;
        }
    }

}
