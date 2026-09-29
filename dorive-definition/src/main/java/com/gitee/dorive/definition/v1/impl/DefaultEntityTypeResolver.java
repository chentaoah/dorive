/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.definition.v1.impl;

import com.gitee.dorive.base.v1.definition.entity.EntityDefinition;
import com.gitee.dorive.base.v1.definition.entity.EntityElement;
import com.gitee.dorive.base.v1.definition.api.EntityTypeResolver;

import java.util.List;

public class DefaultEntityTypeResolver implements EntityTypeResolver {

    @Override
    public List<EntityElement> resolve(Class<?> entityType) {
        EntityDefinitionResolver entityDefinitionResolver = new EntityDefinitionResolver();
        EntityDefinition entityDefinition = entityDefinitionResolver.resolve(entityType);
        EntityElementResolver entityElementResolver = new EntityElementResolver();
        return entityElementResolver.resolve(entityDefinition);
    }

}
