/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
        if (fieldValue instanceof RefImpl) {
            RefImpl<?> refImpl = (RefImpl<?>) fieldValue;
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
