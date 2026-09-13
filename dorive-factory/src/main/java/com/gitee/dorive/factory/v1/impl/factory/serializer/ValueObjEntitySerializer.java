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

package com.gitee.dorive.factory.v1.impl.factory.serializer;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.convert.TypeConverter;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.factory.v1.impl.mapper.FieldMapping;
import com.gitee.dorive.factory.v1.api.EntityMapperManager;
import com.gitee.dorive.factory.v1.impl.mapper.DefaultEntityMapper;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ValueObjEntitySerializer extends DefaultEntitySerializer {

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
            if (targetType == String.class) {
                // 以下情况，不再使用hutool的类型转换（toString）
                if (value instanceof Collection) {
                    return value;
                }
                if (value instanceof Map) {
                    return value;
                }
                // 注意：值对象的子类实例，不会进入该分支
                if (entityMapperManager.isValueObjType(value.getClass())) {
                    return value;
                }
            }
            return converter.convert(targetType, value);
        }));
    }

    @Override
    public Object serialize(Context context, Object object) {
        Object pojo = super.serialize(context, object);

        DefaultEntityMapper defaultEntityMapper = (DefaultEntityMapper) getEntityMapper();
        List<FieldMapping> unmatchedValueObjFields = defaultEntityMapper.getUnmatchedValueObjFields();
        for (FieldMapping fieldMapping : unmatchedValueObjFields) {
            Object valueObj = BeanUtil.getFieldValue(object, fieldMapping.getField());
            valueObj = valueObj != null ? fieldMapping.serialize(valueObj) : null;
            if (valueObj != null) {
                BeanUtil.copyProperties(valueObj, pojo, CopyOptions.create().ignoreNullValue());
            }
        }
        return pojo;
    }
}
