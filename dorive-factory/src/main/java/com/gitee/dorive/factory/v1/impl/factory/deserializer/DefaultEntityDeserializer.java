/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.factory.v1.impl.factory.deserializer;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.gitee.dorive.base.v1.definition.entity.EntityElement;
import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.base.v1.factory.api.entity.EntityDeserializer;
import com.gitee.dorive.base.v1.factory.api.entity.EntityMapper;
import com.gitee.dorive.factory.v1.api.InitializingObject;
import com.gitee.dorive.factory.v1.api.TypeAdapter;
import com.gitee.dorive.factory.v1.impl.adapter.MapTypeAdapter;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DefaultEntityDeserializer implements EntityDeserializer, InitializingObject {

    private EntityElement entityElement;
    private Class<?> entityType;
    private EntityMapper entityMapper;
    private CopyOptions copyOptions;
    private TypeAdapter typeAdapter;

    @Override
    public void initialize() {
        initCopyOptions();
        initTypeAdapter();
        processTypeAdapter();
    }

    private void initCopyOptions() {
        this.copyOptions = CopyOptions.create() //
                .ignoreNullValue() //
                .setFieldNameEditor(alias -> entityMapper.deserialize(alias)) //
                .setFieldValueEditor((field, value) -> {
                    String alias = entityMapper.serialize(field);
                    return entityMapper.deserialize(alias, value);
                });
    }

    protected void initTypeAdapter() {
        this.typeAdapter = (persistent) -> entityType;
    }

    protected void processTypeAdapter() {
        if (typeAdapter instanceof MapTypeAdapter) {
            ((MapTypeAdapter) typeAdapter).initialize(entityElement, entityMapper);
        }
    }

    @Override
    public Object deserialize(Context context, Object object) {
        return BeanUtil.toBean(object, typeAdapter.determineType(object), copyOptions);
    }

}
