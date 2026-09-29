/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.factory.v1.api;

import com.gitee.dorive.base.v1.factory.api.entity.EntityMapper;

import java.lang.reflect.Type;

public interface EntityMapperManager {

    EntityMapper getDatabaseEntityMapper();

    EntityMapper getPojoEntityMapper();

    boolean containValueObj();

    boolean isValueObjType(Type type);

    boolean containMatchedValueObj();

}
