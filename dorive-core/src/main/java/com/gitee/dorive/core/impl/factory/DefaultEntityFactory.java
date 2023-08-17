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
import com.gitee.dorive.api.entity.element.EntityEle;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.executor.Converter;
import com.gitee.dorive.core.api.executor.EntityFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DefaultEntityFactory implements EntityFactory {

    private EntityEle entityEle;
    private Class<?> pojoClass;

    private Map<String, String> aliasFieldMapping;
    private Map<String, Converter> aliasConverterMap;
    private CopyOptions reCopyOptions;

    private Map<String, String> fieldPropMapping;
    private Map<String, Converter> fieldConverterMap;
    private CopyOptions deCopyOptions;

    public void setReCopyOptions(Map<String, String> aliasFieldMapping, Map<String, Converter> aliasConverterMap) {
        this.aliasFieldMapping = aliasFieldMapping;
        this.aliasConverterMap = aliasConverterMap;
        this.reCopyOptions = CopyOptions.create().ignoreNullValue().setFieldMapping(aliasFieldMapping)
                .setFieldValueEditor((name, value) -> {
                    Converter converter = aliasConverterMap.get(name);
                    return converter != null ? converter.reconstitute(name, value) : value;
                });
    }

    public void setDeCopyOptions(Map<String, String> fieldPropMapping, Map<String, Converter> fieldConverterMap) {
        this.fieldPropMapping = fieldPropMapping;
        this.fieldConverterMap = fieldConverterMap;
        this.deCopyOptions = CopyOptions.create().ignoreNullValue().setFieldMapping(fieldPropMapping)
                .setFieldValueEditor((name, value) -> {
                    Converter converter = fieldConverterMap.get(name);
                    return converter != null ? converter.deconstruct(name, value) : value;
                });
    }

    @Override
    public Object reconstitute(Context context, Object persistent) {
        return BeanUtil.toBean(persistent, entityEle.getGenericType(), reCopyOptions);
    }

    @Override
    public Object deconstruct(Context context, Object entity) {
        return BeanUtil.toBean(entity, pojoClass, deCopyOptions);
    }

}
