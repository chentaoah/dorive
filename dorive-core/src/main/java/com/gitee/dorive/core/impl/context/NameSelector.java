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

import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.core.api.context.Selector;
import com.gitee.dorive.core.entity.option.SelectType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = false)
public class NameSelector implements Selector {

    private Set<String> names = Collections.emptySet();
    private Map<String, NameDef> nameDefMap = Collections.emptyMap();

    public NameSelector(String... names) {
        if (names != null && names.length > 0) {
            int size = names.length * 4 / 3 + 1;
            this.names = new LinkedHashSet<>(size);
            this.nameDefMap = new LinkedHashMap<>(size);
            for (String name : names) {
                if (name.contains("(") && name.contains(")")) {
                    String realName = name.substring(0, name.indexOf("("));
                    String propText = name.substring(name.indexOf("(") + 1, name.indexOf(")"));
                    List<String> properties = StrUtil.splitTrim(propText, ",");
                    this.names.add(realName);
                    this.nameDefMap.put(realName, new NameDef(realName, Collections.unmodifiableList(properties)));

                } else {
                    this.names.add(name);
                    this.nameDefMap.put(name, new NameDef(name, Collections.emptyList()));
                }
            }
            this.names = Collections.unmodifiableSet(this.names);
        }
    }

    @Override
    public Set<String> getNames() {
        return names;
    }

    @Override
    public List<String> select(String name) {
        NameDef nameDef = nameDefMap.get(name);
        return nameDef != null && !nameDef.getProperties().isEmpty() ? nameDef.getProperties() : null;
    }

    @Override
    public Map<Class<?>, Object> get() {
        Map<Class<?>, Object> options = new LinkedHashMap<>(3);
        options.put(SelectType.class, SelectType.NAME);
        options.put(Selector.class, this);
        return options;
    }

    @Data
    @AllArgsConstructor
    public static class NameDef {
        private String name;
        private List<String> properties;
    }

}

