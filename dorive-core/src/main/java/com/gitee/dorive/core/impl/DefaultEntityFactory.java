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
package com.gitee.dorive.core.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.gitee.dorive.core.entity.element.EntityElement;
import com.gitee.dorive.core.entity.BoundedContext;
import com.gitee.dorive.core.api.EntityFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DefaultEntityFactory implements EntityFactory {

    protected EntityElement entityElement;
    protected Class<?> pojoClass;
    protected Map<String, String> propAliasMapping;
    protected Map<String, String> aliasPropMapping;

    @Override
    public Object reconstitute(BoundedContext boundedContext, Object persistentObject) {
        CopyOptions copyOptions = CopyOptions.create().ignoreNullValue().setFieldMapping(propAliasMapping);
        return BeanUtil.toBean(persistentObject, entityElement.getGenericType(), copyOptions);
    }

    @Override
    public Object deconstruct(BoundedContext boundedContext, Object entity) {
        CopyOptions copyOptions = CopyOptions.create().ignoreNullValue().setFieldMapping(aliasPropMapping);
        return BeanUtil.toBean(entity, pojoClass, copyOptions);
    }

}
