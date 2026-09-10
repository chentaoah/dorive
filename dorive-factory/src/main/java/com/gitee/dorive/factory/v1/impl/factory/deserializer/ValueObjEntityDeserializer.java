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

package com.gitee.dorive.factory.v1.impl.factory.deserializer;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.convert.TypeConverter;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.factory.v1.impl.mapper.FieldMapping;
import com.gitee.dorive.factory.v1.api.EntityMapperManager;
import com.gitee.dorive.factory.v1.impl.mapper.DefaultEntityMapper;
import com.gitee.dorive.factory.v1.util.TypeUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ValueObjEntityDeserializer extends DefaultEntityDeserializer {

    private EntityMapperManager entityMapperManager;

    // 从hutool源码中拷贝
    protected TypeConverter converter = (type, value) -> {
        if (null == value) {
            return null;
        }
        final String name = value.getClass().getName();
        if (ArrayUtil.contains(new String[]{"cn.hutool.json.JSONObject", "cn.hutool.json.JSONArray"}, name)) {
            return ReflectUtil.invoke(value, "toBean", ObjectUtil.defaultIfNull(type, Object.class));
        }
        return Convert.convertWithCheck(type, value, null, true);
    };

    @Override
    public void initialize() {
        super.initialize();
        if (entityMapperManager.containMatchedValueObj()) {
            resetCopyOptions();
        }
    }

    private void resetCopyOptions() {
        getCopyOptions().setConverter(((targetType, value) -> {
            if (value == null) {
                return null;
            }
            if (value instanceof String) {
                // 以下情况，不再使用hutool的类型转换（toString）
                Class<?> rawType = TypeUtils.getRawType(targetType);
                if (rawType == null) {
                    throw new RuntimeException("The rawType is null!");
                }
                if (Collection.class.isAssignableFrom(rawType)) {
                    return value;
                }
                if (Map.class.isAssignableFrom(rawType)) {
                    return value;
                }
                if (entityMapperManager.isValueObjType(rawType)) {
                    return value;
                }
            }
            return converter.convert(targetType, value);
        }));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object deserialize(Context context, Object object) {
        Object entity = super.deserialize(context, object);

        DefaultEntityMapper defaultEntityMapper = (DefaultEntityMapper) getEntityMapper();
        Map<String, Object> resultMap = (Map<String, Object>) object;
        List<FieldMapping> unmatchedValueObjFields = defaultEntityMapper.getUnmatchedValueObjFields();
        for (FieldMapping fieldMapping : unmatchedValueObjFields) {
            Object valueObj = fieldMapping.deserialize(resultMap);
            if (valueObj != null) {
                BeanUtil.setFieldValue(entity, fieldMapping.getField(), valueObj);
            }
        }
        return entity;
    }
}
