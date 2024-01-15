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

package com.gitee.dorive.core.impl.converter;

import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.api.entity.def.FieldDef;
import com.gitee.dorive.api.entity.element.EntityField;
import com.gitee.dorive.core.api.executor.Converter;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class DefaultConverter implements Converter {

    private EntityField entityField;
    private Map<Object, Object> reMapping = Collections.emptyMap();
    private Map<Object, Object> deMapping = Collections.emptyMap();

    public DefaultConverter(EntityField entityField) {
        this.entityField = entityField;
        FieldDef fieldDef = entityField.getFieldDef();
        Class<?> genericType = entityField.getGenericType();
        String mapExp = fieldDef.getMapExp();
        if (StringUtils.isNotBlank(mapExp)) {
            this.reMapping = new LinkedHashMap<>(8);
            this.deMapping = new LinkedHashMap<>(8);
            List<String> items = StrUtil.splitTrim(mapExp, ",");
            for (String item : items) {
                if (StringUtils.isNotBlank(item)) {
                    List<String> valueValuePair = StrUtil.splitTrim(item, "=");
                    String entityValue = valueValuePair.get(0);
                    String mapValue = valueValuePair.get(1);
                    if (genericType == Integer.class) {
                        reMapping.put(Integer.valueOf(mapValue), Integer.valueOf(entityValue));
                        deMapping.put(Integer.valueOf(entityValue), Integer.valueOf(mapValue));

                    } else if (genericType == String.class) {
                        reMapping.put(mapValue, entityValue);
                        deMapping.put(entityValue, mapValue);
                    }
                }
            }
        }
    }

    @Override
    public Object fieldToAlias(String alias, Object value) {
        return deconstruct(value);
    }

    @Override
    public Object aliasToField(String field, Object value) {
        return reconstitute(value);
    }

    @Override
    public Object fieldToProp(String prop, Object value) {
        return deconstruct(value);
    }

    private Object reconstitute(Object value) {
        if (value == null) {
            return null;
        }
        Object entityValue = reMapping.get(value);
        if (entityValue != null) {
            return entityValue;
        }
        return value;
    }

    private Object deconstruct(Object value) {
        if (value == null) {
            return null;
        }
        Object mapValue = deMapping.get(value);
        if (mapValue != null) {
            return mapValue;
        }
        return value;
    }

}
