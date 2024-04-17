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
import com.gitee.dorive.api.entity.EntityEle;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.factory.EntityFactory;
import com.gitee.dorive.core.api.factory.EntityMapper;
import com.gitee.dorive.core.entity.common.EntityStoreInfo;
import com.gitee.dorive.core.entity.enums.Domain;
import com.gitee.dorive.core.entity.factory.FieldInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DefaultEntityFactory implements EntityFactory {

    private EntityEle entityEle;
    private EntityStoreInfo entityStoreInfo;
    private EntityMapper entityMapper;
    private CopyOptions reCopyOptions;
    private CopyOptions deCopyOptions;

    public void setEntityMapper(EntityMapper entityMapper) {
        this.entityMapper = entityMapper;
        initReCopyOptions();
        initDeCopyOptions();
    }

    private void initReCopyOptions() {
        this.reCopyOptions = CopyOptions.create().ignoreNullValue().setFieldNameEditor(name -> {
            FieldInfo fieldInfo = entityMapper.findField(Domain.DATABASE.name(), name);
            return fieldInfo != null ? fieldInfo.getName() : name;

        }).setFieldValueEditor((name, value) -> {
            FieldInfo fieldInfo = entityMapper.findField(Domain.ENTITY.name(), name);
            return fieldInfo != null ? fieldInfo.reconstitute(Domain.DATABASE.name(), value) : value;
        });
    }

    private void initDeCopyOptions() {
        this.deCopyOptions = CopyOptions.create().ignoreNullValue().setFieldNameEditor(name -> {
            FieldInfo fieldInfo = entityMapper.findField(Domain.ENTITY.name(), name);
            return fieldInfo != null ? fieldInfo.getAlias(Domain.POJO.name()) : name;

        }).setFieldValueEditor((name, value) -> {
            FieldInfo fieldInfo = entityMapper.findField(Domain.POJO.name(), name);
            return fieldInfo != null ? fieldInfo.deconstruct(Domain.POJO.name(), value) : value;
        });
    }

    @Override
    public Object reconstitute(Context context, Object persistent) {
        return BeanUtil.toBean(persistent, entityEle.getGenericType(), reCopyOptions);
    }

    @Override
    public Object deconstruct(Context context, Object entity) {
        return BeanUtil.toBean(entity, entityStoreInfo.getPojoClass(), deCopyOptions);
    }

}
