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

package com.gitee.dorive.core.impl.factory;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.func.Func1;
import cn.hutool.core.lang.func.LambdaUtil;
import com.gitee.dorive.api.entity.core.EntityElement;
import com.gitee.dorive.core.api.factory.EntityAdapter;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class AdaptiveEntityAdapter implements EntityAdapter {

    private EntityElement entityElement;
    private String field;
    private String alias;
    private Map<Object, Class<?>> valueEntityTypeMap;

    public <T> AdaptiveEntityAdapter(EntityElement entityElement, Func1<T, ?> func, Map<Object, Class<?>> valueEntityTypeMap) {
        Assert.notNull(entityElement, "The entityElement cannot be null!");
        Assert.notNull(func, "The func cannot be null!");
        Assert.notEmpty(valueEntityTypeMap, "The valueEntityTypeMap cannot be empty!");
        this.entityElement = entityElement;
        this.field = LambdaUtil.getFieldName(func);
        this.alias = entityElement.toAlias(field);
        this.valueEntityTypeMap = valueEntityTypeMap;
    }

    @Override
    public Class<?> adaptEntityType(Object persistent) {
        Object fieldValue = BeanUtil.getFieldValue(persistent, alias);
        if (fieldValue == null) {
            return entityElement.getGenericType();
        }
        return valueEntityTypeMap.getOrDefault(fieldValue, entityElement.getGenericType());
    }

}
