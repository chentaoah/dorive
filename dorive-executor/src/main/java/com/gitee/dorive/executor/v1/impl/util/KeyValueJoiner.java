/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.executor.v1.impl.util;

import com.gitee.dorive.base.v1.definition.entity.EntityElement;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class KeyValueJoiner extends HashMapJoiner {

    private RepositoryItem repositoryItem;

    public KeyValueJoiner(RepositoryItem repositoryItem, List<Object> entities) {
        super(repositoryItem.isCollection(), entities);
        this.repositoryItem = repositoryItem;
    }

    @Override
    protected void doJoin(Object entity, Object object) {
        EntityElement entityElement = repositoryItem.getEntityElement();
        Object value = entityElement.getValue(entity);
        if (value == null) {
            entityElement.setValue(entity, object);
        }
    }
}
