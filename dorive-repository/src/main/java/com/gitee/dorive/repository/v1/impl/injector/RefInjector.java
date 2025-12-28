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

package com.gitee.dorive.repository.v1.impl.injector;

import cn.hutool.core.util.ReflectUtil;
import com.gitee.dorive.base.v1.executor.api.EntityHandler;
import com.gitee.dorive.repository.v1.api.Ref;
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
        inject();
    }

    private void inject() {
        Field staticRefField = findStaticRefField();
        if (staticRefField != null) {
            Ref<Object> ref = newRefImpl();
            doInject(staticRefField, ref);
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

    @SuppressWarnings("unchecked")
    private Ref<Object> newRefImpl() {
        RefImpl refImpl = new RefImpl((AbstractQueryRepository<Object, Object>) repository, entityHandler);
        refImpl.setEntityElement(repository.getEntityElement());
        refImpl.setOperationFactory(repository.getOperationFactory());
        refImpl.setExecutor(repository.getExecutor());
        return refImpl;
    }

    private void doInject(Field field, Ref<Object> ref) {
        ReflectUtil.setFieldValue(null, field, ref);
    }

}
