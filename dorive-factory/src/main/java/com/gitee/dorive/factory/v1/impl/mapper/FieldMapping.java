/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.factory.v1.impl.mapper;

import com.gitee.dorive.factory.v1.api.ValueConverter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FieldMapping implements ValueConverter {
    private String field;
    private String alias;
    private ValueConverter valueConverter;

    @Override
    public Object deserialize(Object value) {
        return valueConverter == null ? value : valueConverter.deserialize(value);
    }

    @Override
    public Object serialize(Object value) {
        return valueConverter == null ? value : valueConverter.serialize(value);
    }
}
