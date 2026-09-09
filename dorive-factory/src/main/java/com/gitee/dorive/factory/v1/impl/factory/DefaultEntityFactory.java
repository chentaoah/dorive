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

package com.gitee.dorive.factory.v1.impl.factory;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.gitee.dorive.base.v1.definition.entity.EntityElement;
import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.base.v1.factory.api.EntityFactory;
import com.gitee.dorive.base.v1.factory.api.EntityTransformer;
import com.gitee.dorive.base.v1.factory.api.FieldAliasMapping;
import com.gitee.dorive.factory.v1.api.TypeAdapter;
import com.gitee.dorive.factory.v1.impl.adapter.MapTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DefaultEntityFactory implements EntityFactory {

    private EntityElement entityElement;
    // 类型
    private Class<?> reType;
    private Class<?> deType;
    // 转换器
    private EntityTransformer reEntityTransformer;
    private EntityTransformer deEntityTransformer;
    // 序列化配置
    private CopyOptions reCopyOptions;
    private CopyOptions deCopyOptions;
    // 适配器
    private TypeAdapter typeAdapter;

    public void initialize() {
        initReCopyOptions();
        initDeCopyOptions();
        initTypeAdapter();
        processTypeAdapter();
    }

    private void initReCopyOptions() {
        this.reCopyOptions = CopyOptions.create().ignoreNullValue().setFieldNameEditor(alias -> {
            FieldAliasMapping fieldAliasMappingByAlias = reEntityTransformer.getFieldAliasMappingByAlias(alias);
            return fieldAliasMappingByAlias != null ? fieldAliasMappingByAlias.getField() : alias;

        }).setFieldValueEditor((field, value) -> {
            FieldAliasMapping fieldAliasMappingByField = reEntityTransformer.getFieldAliasMappingByField(field);
            return fieldAliasMappingByField != null ? fieldAliasMappingByField.reconstitute(value) : value;
        });
    }

    private void initDeCopyOptions() {
        this.deCopyOptions = CopyOptions.create().ignoreNullValue().setFieldNameEditor(field -> {
            FieldAliasMapping fieldAliasMappingByField = deEntityTransformer.getFieldAliasMappingByField(field);
            return fieldAliasMappingByField != null ? fieldAliasMappingByField.getAlias() : field;

        }).setFieldValueEditor((alias, value) -> {
            FieldAliasMapping fieldAliasMappingByAlias = deEntityTransformer.getFieldAliasMappingByAlias(alias);
            return fieldAliasMappingByAlias != null ? fieldAliasMappingByAlias.deconstruct(value) : value;
        });
    }

    protected void initTypeAdapter() {
        this.typeAdapter = (persistent) -> reType;
    }

    protected void processTypeAdapter() {
        if (typeAdapter instanceof MapTypeAdapter) {
            ((MapTypeAdapter) typeAdapter).initialize(entityElement, reEntityTransformer);
        }
    }

    @Override
    public Object deserialize(Context context, Object object) {
        return BeanUtil.toBean(object, typeAdapter.determineType(object), reCopyOptions);
    }

    @Override
    public Object serialize(Context context, Object object) {
        return BeanUtil.toBean(object, deType, deCopyOptions);
    }

}
