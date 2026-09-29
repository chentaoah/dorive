/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.factory.v1.impl.mapper;

import com.gitee.dorive.base.v1.factory.api.entity.EntityMapper;
import com.gitee.dorive.factory.v1.api.EntityMapperManager;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Type;
import java.util.Set;

@Getter
@AllArgsConstructor
public class DefaultEntityMapperManager implements EntityMapperManager {
    private final EntityMapper databaseEntityMapper;
    private final EntityMapper pojoEntityMapper;
    private final Set<Type> valueObjTypes;
    private final boolean containMatchedValueObj;

    @Override
    public boolean containValueObj() {
        return valueObjTypes != null && !valueObjTypes.isEmpty();
    }

    @Override
    public boolean isValueObjType(Type type) {
        return valueObjTypes.contains(type);
    }

    @Override
    public boolean containMatchedValueObj() {
        return containMatchedValueObj;
    }
}
