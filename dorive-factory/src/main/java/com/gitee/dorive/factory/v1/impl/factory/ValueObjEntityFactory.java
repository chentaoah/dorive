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

package com.gitee.dorive.factory.v1.impl.factory;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.convert.TypeConverter;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.base.v1.factory.api.EntityTransformer;
import com.gitee.dorive.factory.v1.api.EntityTransformerManager;
import com.gitee.dorive.base.v1.factory.api.FieldAliasMapping;
import com.gitee.dorive.factory.v1.util.TypeUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ValueObjEntityFactory extends DefaultEntityFactory {

    private EntityTransformerManager entityTransformerManager;

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

    public void initialize() {
        if (entityTransformerManager.containMatchedValueObj()) {
            setReCopyOptions();
            setDeCopyOptions();
        }
    }

    private void setReCopyOptions() {
        DefaultDeserializer deserializer = (DefaultDeserializer) getDeserializer();
        CopyOptions copyOptions = deserializer.getCopyOptions();
        copyOptions.setConverter(((targetType, value) -> {
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
                if (entityTransformerManager.isValueObjType(rawType)) {
                    return value;
                }
            }
            return converter.convert(targetType, value);
        }));
    }

    private void setDeCopyOptions() {
        DefaultSerializer serializer = (DefaultSerializer) getSerializer();
        CopyOptions copyOptions = serializer.getCopyOptions();
        copyOptions.setConverter(((targetType, value) -> {
            if (value == null) {
                return null;
            }
            if (targetType == String.class) {
                // 以下情况，不再使用hutool的类型转换（toString）
                if (value instanceof Collection) {
                    return value;
                }
                if (value instanceof Map) {
                    return value;
                }
                // 注意：值对象的子类实例，不会进入该分支
                if (entityTransformerManager.isValueObjType(value.getClass())) {
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

        DefaultDeserializer deserializer = (DefaultDeserializer) getDeserializer();
        EntityTransformer entityTransformer = deserializer.getEntityTransformer();
        Map<String, Object> resultMap = (Map<String, Object>) object;
        List<FieldAliasMapping> unmatchedValueObjFields = entityTransformer.getUnmatchedValueObjFields();
        for (FieldAliasMapping fieldAliasMapping : unmatchedValueObjFields) {
            Object valueObj = fieldAliasMapping.reconstitute(resultMap);
            if (valueObj != null) {
                BeanUtil.setFieldValue(entity, fieldAliasMapping.getField(), valueObj);
            }
        }
        return entity;
    }

    @Override
    public Object serialize(Context context, Object object) {
        Object pojo = super.serialize(context, object);

        DefaultSerializer serializer = (DefaultSerializer) getSerializer();
        EntityTransformer entityTransformer = serializer.getEntityTransformer();
        List<FieldAliasMapping> unmatchedValueObjFields = entityTransformer.getUnmatchedValueObjFields();
        for (FieldAliasMapping fieldAliasMapping : unmatchedValueObjFields) {
            Object valueObj = BeanUtil.getFieldValue(object, fieldAliasMapping.getField());
            valueObj = valueObj != null ? fieldAliasMapping.deconstruct(valueObj) : null;
            if (valueObj != null) {
                BeanUtil.copyProperties(valueObj, pojo, CopyOptions.create().ignoreNullValue());
            }
        }
        return pojo;
    }

}
