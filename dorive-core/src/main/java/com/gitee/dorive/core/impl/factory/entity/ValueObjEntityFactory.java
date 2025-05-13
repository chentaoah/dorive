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

package com.gitee.dorive.core.impl.factory.entity;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.convert.TypeConverter;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.json.JSONUtil;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.mapper.EntityMapper;
import com.gitee.dorive.core.api.mapper.EntityMappers;
import com.gitee.dorive.core.api.mapper.FieldMapper;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ValueObjEntityFactory extends DefaultEntityFactory {

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
    public void setEntityMappers(EntityMappers entityMappers, EntityMapper reEntityMapper, EntityMapper deEntityMapper) {
        super.setEntityMappers(entityMappers, reEntityMapper, deEntityMapper);
        List<FieldMapper> matchedValueObjFields = entityMappers.getMatchedValueObjFields();
        if (!matchedValueObjFields.isEmpty()) {
            setReCopyOptions();
            setDeCopyOptions();
        }
    }

    private void setReCopyOptions() {
        // 如果是值对象，则跳过hutool的类型转换
        EntityMappers entityMappers = getEntityMappers();
        getReCopyOptions().setConverter(((targetType, value) -> {
            if (value == null) {
                return null;
            }
            if (value instanceof String) {
                if (targetType instanceof ParameterizedType) {
                    targetType = ((ParameterizedType) targetType).getActualTypeArguments()[0];
                }
                if (entityMappers.isValueObjType(targetType)) {
                    return value;
                }
            }
            return converter.convert(targetType, value);
        }));
    }

    private void setDeCopyOptions() {
        // 如果是值对象，则跳过hutool的类型转换
        EntityMappers entityMappers = getEntityMappers();
        getDeCopyOptions().setConverter(((targetType, value) -> {
            if (value == null) {
                return null;
            }
            if (targetType == String.class) {
                // 运行时类型擦除，若优化需重写hutool逻辑
                if (value instanceof Collection) {
                    return JSONUtil.toJsonStr(value);
                }
                if (entityMappers.isValueObjType(value.getClass())) {
                    return value;
                }
            }
            return converter.convert(targetType, value);
        }));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object reconstitute(Context context, Object persistent) {
        Object entity = super.reconstitute(context, persistent);
        Map<String, Object> resultMap = (Map<String, Object>) persistent;
        EntityMappers entityMappers = getEntityMappers();
        List<FieldMapper> unmatchedValueObjFields = entityMappers.getUnmatchedValueObjFields();
        for (FieldMapper fieldMapper : unmatchedValueObjFields) {
            Object valueObj = fieldMapper.reconstitute(resultMap);
            if (valueObj != null) {
                BeanUtil.setFieldValue(entity, fieldMapper.getField(), valueObj);
            }
        }
        return entity;
    }

    @Override
    public Object deconstruct(Context context, Object entity) {
        Object pojo = super.deconstruct(context, entity);
        EntityMappers entityMappers = getEntityMappers();
        List<FieldMapper> unmatchedValueObjFields = entityMappers.getUnmatchedValueObjFields();
        for (FieldMapper fieldMapper : unmatchedValueObjFields) {
            Object valueObj = BeanUtil.getFieldValue(entity, fieldMapper.getField());
            valueObj = valueObj != null ? fieldMapper.deconstruct(valueObj) : null;
            if (valueObj != null) {
                BeanUtil.copyProperties(valueObj, pojo, CopyOptions.create().ignoreNullValue());
            }
        }
        return pojo;
    }

}
