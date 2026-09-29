/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.factory.v1.impl.converter;

import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.base.v1.definition.entity.FieldDefinition;
import com.gitee.dorive.base.v1.definition.def.FieldDef;
import com.gitee.dorive.factory.v1.api.ValueConverter;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class MapExpValueConverter implements ValueConverter {

    private FieldDefinition fieldDefinition;
    private Map<Object, Object> reMap = Collections.emptyMap();
    private Map<Object, Object> deMap = Collections.emptyMap();

    public MapExpValueConverter(FieldDefinition fieldDefinition) {
        this.fieldDefinition = fieldDefinition;
        FieldDef fieldDef = fieldDefinition.getFieldDef();
        Class<?> genericType = fieldDefinition.getGenericType();
        String expression = fieldDef.getExpression();
        if (StringUtils.isNotBlank(expression)) {
            this.reMap = new LinkedHashMap<>(8);
            this.deMap = new LinkedHashMap<>(8);
            List<String> items = StrUtil.splitTrim(expression, ",");
            for (String item : items) {
                if (StringUtils.isNotBlank(item)) {
                    List<String> valueValuePair = StrUtil.splitTrim(item, "=");
                    String entityValue = valueValuePair.get(0);
                    String mapValue = valueValuePair.get(1);
                    if (genericType == Integer.class) {
                        reMap.put(Integer.valueOf(mapValue), Integer.valueOf(entityValue));
                        deMap.put(Integer.valueOf(entityValue), Integer.valueOf(mapValue));

                    } else if (genericType == String.class) {
                        reMap.put(mapValue, entityValue);
                        deMap.put(entityValue, mapValue);
                    }
                }
            }
        }
    }

    public Object deserialize(Object value) {
        if (value == null) {
            return null;
        }
        Object entityValue = reMap.get(value);
        if (entityValue != null) {
            return entityValue;
        }
        return value;
    }

    public Object serialize(Object value) {
        if (value == null) {
            return null;
        }
        Object mapValue = deMap.get(value);
        if (mapValue != null) {
            return mapValue;
        }
        return value;
    }

}
