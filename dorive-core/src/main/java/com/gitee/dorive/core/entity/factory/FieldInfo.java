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

package com.gitee.dorive.core.entity.factory;

import com.gitee.dorive.core.api.factory.Converter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class FieldInfo {

    private String domain;
    private String name;
    private Map<String, AliasInfo> aliasInfoMap;

    public void addAliasInfo(AliasInfo aliasInfo) {
        aliasInfoMap.put(aliasInfo.getDomain(), aliasInfo);
    }

    public String getAlias(String domain) {
        AliasInfo aliasInfo = aliasInfoMap.get(domain);
        return aliasInfo.getName();
    }

    public Object reconstitute(String domain, Object value) {
        AliasInfo aliasInfo = aliasInfoMap.get(domain);
        Converter converter = aliasInfo.getConverter();
        if (converter != null) {
            return converter.reconstitute(value);
        }
        return value;
    }

    public Object deconstruct(String domain, Object value) {
        AliasInfo aliasInfo = aliasInfoMap.get(domain);
        Converter converter = aliasInfo.getConverter();
        if (converter != null) {
            return converter.deconstruct(value);
        }
        return value;
    }

}
