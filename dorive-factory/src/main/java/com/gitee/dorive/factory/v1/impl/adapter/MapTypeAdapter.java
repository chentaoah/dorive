/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.factory.v1.impl.adapter;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.func.Func1;
import cn.hutool.core.lang.func.LambdaUtil;
import com.gitee.dorive.base.v1.definition.entity.EntityElement;
import com.gitee.dorive.factory.v1.api.TypeAdapter;
import com.gitee.dorive.base.v1.factory.api.entity.EntityMapper;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class MapTypeAdapter implements TypeAdapter {

    private String field;
    private Map<Object, Class<?>> valueEntityTypeMap;
    private EntityElement entityElement;
    private EntityMapper entityMapper;
    private String alias;

    public <T> MapTypeAdapter(Func1<T, ?> func, Map<Object, Class<?>> valueEntityTypeMap) {
        Assert.notNull(func, "The func cannot be null!");
        Assert.notEmpty(valueEntityTypeMap, "The valueEntityTypeMap cannot be empty!");
        this.field = LambdaUtil.getFieldName(func);
        this.valueEntityTypeMap = valueEntityTypeMap;
    }

    public void initialize(EntityElement entityElement, EntityMapper entityMapper) {
        this.entityElement = entityElement;
        this.entityMapper = entityMapper;
        this.alias = entityMapper.serialize(field);
    }

    @Override
    public Class<?> determineType(Object persistent) {
        Object fieldValue = BeanUtil.getFieldValue(persistent, alias);
        if (fieldValue == null) {
            return entityElement.getGenericType();
        }
        return valueEntityTypeMap.getOrDefault(fieldValue, entityElement.getGenericType());
    }

}
