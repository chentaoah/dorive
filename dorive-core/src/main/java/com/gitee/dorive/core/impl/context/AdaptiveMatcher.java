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

import com.gitee.dorive.core.api.context.Matcher;
import com.gitee.dorive.core.api.context.Options;
import com.gitee.dorive.core.api.context.Selector;
import com.gitee.dorive.core.entity.enums.MatcherType;
import com.gitee.dorive.core.impl.repository.CommonRepository;
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
public class AdaptiveMatcher implements Matcher {

    private CommonRepository repository;
    private Map<MatcherType, Matcher> matcherTypeMatcherMap = new LinkedHashMap<>(6);

    public AdaptiveMatcher(CommonRepository repository) {
        this.repository = repository;
        this.matcherTypeMatcherMap.put(MatcherType.NONE, options -> false);
        this.matcherTypeMatcherMap.put(MatcherType.ROOT, options -> repository.isRoot());
        this.matcherTypeMatcherMap.put(MatcherType.ALL, options -> true);
        this.matcherTypeMatcherMap.put(MatcherType.SELECTOR, new SelectorMatcher());
    }

    @Override
    public boolean matches(Options options) {
        MatcherType matcherType = options.getOption(MatcherType.class);
        if (matcherType != null) {
            Matcher matcher = matcherTypeMatcherMap.get(matcherType);
            if (matcher != null) {
                return matcher.matches(options);
            }
        }
        return false;
    }

    private class SelectorMatcher implements Matcher {
        @Override
        public boolean matches(Options options) {
            Selector selector = options.getOption(Selector.class);
            if (selector != null) {
                Set<String> names = selector.getNames();
                String name = repository.getName();
                return names.contains(name);
            }
            return false;
        }
    }

}
