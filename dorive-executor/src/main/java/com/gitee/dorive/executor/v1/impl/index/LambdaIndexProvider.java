/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.executor.v1.impl.index;

import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.support.LambdaMeta;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.gitee.dorive.base.v1.definition.entity.EntityElement;
import com.gitee.dorive.base.v1.definition.entity.Field;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import com.gitee.dorive.executor.v1.api.IndexProvider;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.reflection.property.PropertyNamer;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class LambdaIndexProvider implements IndexProvider {

    private Class<?> type;
    private List<java.lang.reflect.Field> fields;

    public LambdaIndexProvider(Class<?> type, List<java.lang.reflect.Field> fields) {
        this.type = type;
        this.fields = fields;
    }

    public <T> LambdaIndexProvider and(SFunction<T, ?> function) {
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
