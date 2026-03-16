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

package com.gitee.dorive.executor.v1.impl.matcher;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.base.v1.executor.api.Matcher;
import com.gitee.dorive.base.v1.executor.api.Selector;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import com.gitee.dorive.executor.v1.impl.selector.DefaultSelector;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class NameMatcher implements Matcher {

    private List<String> names;
    private List<Selector> selectors;

    public NameMatcher(String... strings) {
        Assert.notEmpty(strings, "The strings cannot be empty!");
        List<String> names = new ArrayList<>(strings.length);
        List<Selector> selectors = new ArrayList<>(strings.length);
        for (String str : strings) {
            String name = str;
            Selector selector = null;
            if (str.contains("(") && str.contains(")")) {
                name = StrUtil.subBefore(str, "(", false);
                selector = new DefaultSelector(StrUtil.subBetween(str, "(", ")"));
            }
            names.add(name);
            selectors.add(selector);
        }
        this.names = Collections.unmodifiableList(names);
        this.selectors = Collections.unmodifiableList(selectors);
    }

    @Override
    public int indexOf(RepositoryItem repositoryItem) {
        return names.indexOf(repositoryItem.getName());
    }

}

