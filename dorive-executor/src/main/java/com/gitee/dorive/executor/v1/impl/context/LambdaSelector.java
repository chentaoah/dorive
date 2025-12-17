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

package com.gitee.dorive.executor.v1.impl.context;

import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.support.LambdaMeta;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.gitee.dorive.base.v1.common.entity.EntityElement;
import com.gitee.dorive.base.v1.common.entity.Field;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.reflection.property.PropertyNamer;

import java.util.LinkedHashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = false)
public class LambdaSelector extends AbstractSelector {

    private Class<?> type;
    private Set<java.lang.reflect.Field> fields;

    public LambdaSelector(Class<?> type) {
        this.type = type;
        this.fields = new LinkedHashSet<>(8);
    }

    public <T> LambdaSelector and(SFunction<T, ?> function) {
        LambdaMeta meta = LambdaUtils.extract(function);
        Class<?> instantiatedClass = meta.getInstantiatedClass();
        String fieldName = PropertyNamer.methodToProperty(meta.getImplMethodName());
        java.lang.reflect.Field field = ReflectUtil.getField(instantiatedClass, fieldName);
        fields.add(field);
        return this;
    }

    @Override
    public boolean matches(RepositoryItem repositoryItem) {
        if (repositoryItem.isRoot() && type.equals(repositoryItem.getEntityClass())) {
            return true;
        }
        EntityElement entityElement = repositoryItem.getEntityElement();
        Field field = entityElement.getField();
        if (field != null) {
            java.lang.reflect.Field javaField = field.getField();
            return fields.contains(javaField);
        }
        return false;
    }

}
