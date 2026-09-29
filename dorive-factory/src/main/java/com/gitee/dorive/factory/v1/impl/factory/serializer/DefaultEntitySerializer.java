/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.factory.v1.impl.factory.serializer;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.base.v1.factory.api.entity.EntityMapper;
import com.gitee.dorive.base.v1.factory.api.entity.EntitySerializer;
import com.gitee.dorive.factory.v1.api.InitializingObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DefaultEntitySerializer implements EntitySerializer, InitializingObject {

    private Class<?> pojoType;
    private EntityMapper entityMapper;
    private CopyOptions copyOptions;

    @Override
    public void initialize() {
        initCopyOptions();
    }

    private void initCopyOptions() {
        this.copyOptions = CopyOptions.create() //
                .ignoreNullValue() //
                .setFieldNameEditor(field -> entityMapper.serialize(field)) //
                .setFieldValueEditor((alias, value) -> {
                    String field = entityMapper.deserialize(alias);
                    return entityMapper.serialize(field, value);
                });
    }

    @Override
    public Object serialize(Context context, Object object) {
        return BeanUtil.toBean(object, pojoType, copyOptions);
    }

}
