/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.factory.v1.impl.converter;

import cn.hutool.core.bean.BeanUtil;
import com.gitee.dorive.factory.v1.api.ValueConverter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MapValueConverter implements ValueConverter {

    private Class<?> entityClass;

    @Override
    public Object deserialize(Object value) {
        return BeanUtil.toBean(value, entityClass);
    }

    @Override
    public Object serialize(Object value) {
        return BeanUtil.beanToMap(value);
    }

}
