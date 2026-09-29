/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.repository.v1.impl.ref;

import cn.hutool.core.util.ReflectUtil;
import com.gitee.dorive.base.v1.executor.api.EntityHandler;
import com.gitee.dorive.repository.v1.impl.repository.AbstractQueryRepository;
import lombok.Data;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@Data
public class RefInjector {

    private AbstractQueryRepository<?, ?> repository;
    private EntityHandler entityHandler;
    private Class<?> entityClass;

    public RefInjector(AbstractQueryRepository<?, ?> repository, EntityHandler entityHandler, Class<?> entityClass) {
        this.repository = repository;
        this.entityHandler = entityHandler;
        this.entityClass = entityClass;
    }

    public void inject() {
        Field field = findStaticRefField();
        if (field == null) {
            return;
        }
        Object fieldValue = ReflectUtil.getStaticFieldValue(field);
        if (fieldValue instanceof RefImpl<?> refImpl) {
            if (!refImpl.isInitialized()) {
                initialize(refImpl);
            }
        } else {
            RefImpl<?> refImpl = new RefImpl<>();
            initialize(refImpl);
            doInject(field, refImpl);
        }
    }

    private Field findStaticRefField() {
        try {
            Field field = entityClass.getDeclaredField("ref");
            return Modifier.isStatic(field.getModifiers()) ? field : null;
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void initialize(RefImpl<?> refImpl) {
        refImpl.setRepository((AbstractQueryRepository) repository);
        refImpl.setEntityHandler(entityHandler);
        refImpl.setInitialized(true);
    }

    private void doInject(Field field, RefImpl<?> refImpl) {
        ReflectUtil.setFieldValue(null, field, refImpl);
    }

}
