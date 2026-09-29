/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.event.v1.entity;

import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.base.v1.executor.entity.eop.EntityOp;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

import java.util.List;

@Getter
@Setter
public abstract class BaseEvent<T> implements ResolvableTypeProvider {
    private boolean root;
    private Class<?> entityClass;
    private Context context;
    private EntityOp entityOp;

    @Override
    public ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(getClass(), ResolvableType.forClass(entityClass));
    }

    @SuppressWarnings("unchecked")
    public List<T> getEntities() {
        return (List<T>) entityOp.getEntities();
    }
}
