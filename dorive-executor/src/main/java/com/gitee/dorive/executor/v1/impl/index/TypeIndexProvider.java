/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.executor.v1.impl.index;

import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import com.gitee.dorive.executor.v1.api.IndexProvider;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = false)
public class TypeIndexProvider implements IndexProvider {

    private List<Class<?>> types;

    public TypeIndexProvider(Class<?>... types) {
        this.types = Arrays.stream(types).collect(Collectors.toList());
    }

    @Override
    public int indexOf(RepositoryItem repositoryItem) {
        return types.indexOf(repositoryItem.getEntityClass());
    }
}
