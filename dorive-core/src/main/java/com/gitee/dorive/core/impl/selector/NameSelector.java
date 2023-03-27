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

package com.gitee.dorive.core.impl.selector;

import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.api.entity.def.EntityDef;
import com.gitee.dorive.core.repository.CommonRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public class NameSelector extends AbstractSelector {

    public static final NameSelector EMPTY_SELECTOR = new NameSelector();

    private boolean wildcard;
    private Map<String, NameDef> nameDefMap = Collections.emptyMap();

    public NameSelector(String... names) {
        if (names != null && names.length > 0) {
            this.nameDefMap = new LinkedHashMap<>(names.length * 4 / 3 + 1);
            for (String name : names) {
                if ("*".equals(name)) {
                    this.wildcard = true;
                } else {
                    if (name.contains("(") && name.contains(")")) {
                        String realName = name.substring(0, name.indexOf("("));
                        String propertiesText = name.substring(name.indexOf("(") + 1, name.indexOf(")"));
                        List<String> properties = StrUtil.splitTrim(propertiesText, ",");
                        nameDefMap.put(realName, new NameDef(realName, properties));

                    } else {
                        nameDefMap.put(name, new NameDef(name, Collections.emptyList()));
                    }
                }
            }
        }
    }

    @Override
    public boolean matches(Context context, CommonRepository repository) {
        if (wildcard) {
            return true;
        }
        EntityDef entityDef = repository.getEntityDef();
        String name = entityDef.getName();
        return StringUtils.isBlank(name) || nameDefMap.containsKey(name);
    }

    @Override
    public List<String> selectColumns(Context context, CommonRepository repository) {
        EntityDef entityDef = repository.getEntityDef();
        String name = entityDef.getName();
        NameDef nameDef = nameDefMap.get(name);
        return nameDef != null && !nameDef.getProperties().isEmpty() ? nameDef.getProperties() : null;
    }

    @Data
    @AllArgsConstructor
    public static class NameDef {
        private String name;
        private List<String> properties;
    }

}

