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

package com.gitee.dorive.simple.impl;

import cn.hutool.core.util.ReflectUtil;
import com.gitee.dorive.core.api.EntityHandler;
import com.gitee.dorive.core.repository.AbstractRepository;
import com.gitee.dorive.simple.api.Ref;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@Data
@AllArgsConstructor
public class RefInjector {

    private AbstractRepository<?, ?> repository;
    private EntityHandler entityHandler;
    private Class<?> entityClass;

    public Field getField() {
        try {
            Field field = entityClass.getDeclaredField("ref");
            return Modifier.isStatic(field.getModifiers()) ? field : null;
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public Ref<Object> createRef() {
        RefImpl refImpl = new RefImpl((AbstractRepository<Object, Object>) repository, entityHandler);
        refImpl.setEntityDef(repository.getEntityDef());
        refImpl.setEntityEle(repository.getEntityEle());
        refImpl.setOperationFactory(repository.getOperationFactory());
        refImpl.setExecutor(repository.getExecutor());
        return refImpl;
    }

    public void inject(Field field, Ref<Object> ref) {
        ReflectUtil.setFieldValue(null, field, ref);
    }

}
