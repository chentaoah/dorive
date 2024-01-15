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
import com.gitee.dorive.core.api.executor.EntityFactory;
import com.gitee.dorive.core.api.executor.FieldsMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DefaultEntityFactory implements EntityFactory {

    private EntityEle entityEle;
    private Class<?> pojoClass;
    private FieldsMapper fieldsMapper;
    private CopyOptions reCopyOptions;
    private CopyOptions deCopyOptions;

    public void setFieldsMapper(FieldsMapper fieldsMapper) {
        this.fieldsMapper = fieldsMapper;
        initReCopyOptions();
        initDeCopyOptions();
    }

    private void initReCopyOptions() {
        this.reCopyOptions = CopyOptions.create().ignoreNullValue().setFieldNameEditor(fieldsMapper::aliasToField);
        if (fieldsMapper.hasConverter()) {
            this.reCopyOptions.setFieldValueEditor((field, value) -> fieldsMapper.aliasToField(field, value));
        }
    }

    private void initDeCopyOptions() {
        this.deCopyOptions = CopyOptions.create().ignoreNullValue().setFieldNameEditor(fieldsMapper::fieldToProp);
        if (fieldsMapper.hasConverter()) {
            this.deCopyOptions.setFieldValueEditor((prop, value) -> fieldsMapper.fieldToProp(prop, value));
        }
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
