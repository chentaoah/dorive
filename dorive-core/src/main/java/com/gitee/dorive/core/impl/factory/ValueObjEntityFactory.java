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

package com.gitee.dorive.core.impl.factory;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.factory.EntityMapper;
import com.gitee.dorive.core.entity.enums.Domain;
import com.gitee.dorive.core.entity.factory.FieldConverter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValueObjEntityFactory extends DefaultEntityFactory {

    @Override
    @SuppressWarnings("unchecked")
    public Object reconstitute(Context context, Object persistent) {
        Map<String, Object> resultMap = (Map<String, Object>) persistent;
        EntityMapper entityMapper = getEntityMapper();
        List<FieldConverter> fieldConverters = entityMapper.getValueObjFields();
        for (FieldConverter fieldConverter : fieldConverters) {
            String name = fieldConverter.getName(Domain.DATABASE.name());
            if (!resultMap.containsKey(name)) {
                resultMap.put(name, new HashMap<>(resultMap));
            }
        }
        return super.reconstitute(context, persistent);
    }

    @Override
    public Object deconstruct(Context context, Object entity) {
        Object pojo = super.deconstruct(context, entity);
        EntityMapper entityMapper = getEntityMapper();
        List<FieldConverter> fieldConverters = entityMapper.getValueObjFields();
        for (FieldConverter fieldConverter : fieldConverters) {
            String name = fieldConverter.getName(Domain.POJO.name());
            if (name == null) {
                Object valueObj = BeanUtil.getFieldValue(entity, fieldConverter.getName());
                if (valueObj != null) {
                    valueObj = fieldConverter.deconstruct(valueObj);
                    BeanUtil.copyProperties(valueObj, pojo, CopyOptions.create().ignoreNullValue());
                }
            }
        }
        return pojo;
    }

}
