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

package com.gitee.dorive.api.entity.core;

import cn.hutool.core.util.ReflectUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EntityElement extends FieldEntityDefinition {
    private String accessPath;
    private Map<String, String> fieldAliasMapping;

    public boolean isRoot() {
        return "/".equals(accessPath);
    }

    public Object getValue(Object entity) {
        return ReflectUtil.getFieldValue(entity, getFieldName());
    }

    public void setValue(Object entity, Object value) {
        ReflectUtil.setFieldValue(entity, getFieldName(), value);
    }

    public Object getPrimaryKey(Object entity) {
        return ReflectUtil.getFieldValue(entity, getPrimaryKey());
    }

    public void setPrimaryKey(Object entity, Object value) {
        ReflectUtil.setFieldValue(entity, getPrimaryKey(), value);
    }
}
