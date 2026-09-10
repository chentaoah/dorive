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

package com.gitee.dorive.base.v1.executor.impl.selector;

import com.gitee.dorive.base.v1.executor.api.Matcher;
import com.gitee.dorive.base.v1.executor.api.Selection;
import com.gitee.dorive.base.v1.executor.api.Selector;
import com.gitee.dorive.base.v1.executor.impl.matcher.AllMatcher;
import com.gitee.dorive.base.v1.executor.impl.matcher.NoneMatcher;
import com.gitee.dorive.base.v1.executor.impl.matcher.RootMatcher;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DefaultSelector implements Selector {

    public static final Selector NONE_SELECTOR = new DefaultSelector(new NoneMatcher());
    public static final Selector ROOT_SELECTOR = new DefaultSelector(new RootMatcher());
    public static final Selector ALL_SELECTOR = new DefaultSelector(new AllMatcher());

    private Matcher matcher;
    private List<Selection> selections;

    public DefaultSelector(Matcher matcher) {
        this.matcher = matcher;
    }

    @Override
    public boolean matches(RepositoryItem repositoryItem) {
        return matcher != null && matcher.matches(repositoryItem);
    }

    @Override
    public List<String> select(RepositoryItem repositoryItem) {
        if (matcher != null && selections != null) {
            int index = matcher.indexOf(repositoryItem);
            if (index >= 0 && index < selections.size()) {
                Selection selection = selections.get(index);
                if (selection != null) {
                    return selection.select();
                }
            }
        }
        return null;
    }

}
