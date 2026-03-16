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

package com.gitee.dorive.executor.v1.impl.matcher;

import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.support.LambdaMeta;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.gitee.dorive.base.v1.common.entity.EntityElement;
import com.gitee.dorive.base.v1.common.entity.Field;
import com.gitee.dorive.base.v1.core.entity.ctx.GenericOptions;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.reflection.property.PropertyNamer;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class LambdaMatcher extends GenericOptions {

    private Class<?> type;
    private List<java.lang.reflect.Field> fields;

    public LambdaMatcher(Class<?> type) {
        this.type = type;
        this.fields = new ArrayList<>(4);
    }

    public LambdaMatcher(Class<?> type, List<java.lang.reflect.Field> fields) {
        this.type = type;
        this.fields = fields;
    }

    public <T> LambdaMatcher and(SFunction<T, ?> function) {
        LambdaMeta meta = LambdaUtils.extract(function);
        Class<?> instantiatedClass = meta.getInstantiatedClass();
        String fieldName = PropertyNamer.methodToProperty(meta.getImplMethodName());
        java.lang.reflect.Field field = ReflectUtil.getField(instantiatedClass, fieldName);
        fields.add(field);
        return this;
    }

    @Override
    public int indexOf(RepositoryItem repositoryItem) {
        if (repositoryItem.isRoot() && type.equals(repositoryItem.getEntityClass())) {
            return 0;
        }
        EntityElement entityElement = repositoryItem.getEntityElement();
        Field field = entityElement.getField();
        if (field != null) {
            java.lang.reflect.Field javaField = field.getField();
            int index = fields.indexOf(javaField);
            if (index >= 0) {
                return index + 1;
            }
        }
        return -1;
    }

}
