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
import com.gitee.dorive.base.v1.factory.api.Deserializer;
import com.gitee.dorive.base.v1.factory.api.EntityTransformer;
import com.gitee.dorive.base.v1.factory.api.FieldAliasMapping;
import com.gitee.dorive.factory.v1.api.TypeAdapter;
import com.gitee.dorive.factory.v1.impl.adapter.MapTypeAdapter;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DefaultDeserializer implements Deserializer {

    private EntityElement entityElement;
    private Class<?> type;
    private EntityTransformer entityTransformer;
    private CopyOptions copyOptions;
    private TypeAdapter typeAdapter;

    public void initialize() {
        initReCopyOptions();
        initTypeAdapter();
        processTypeAdapter();
    }

    private void initReCopyOptions() {
        this.copyOptions = CopyOptions.create().ignoreNullValue().setFieldNameEditor(alias -> {
            FieldAliasMapping fieldAliasMappingByAlias = entityTransformer.getFieldAliasMappingByAlias(alias);
            return fieldAliasMappingByAlias != null ? fieldAliasMappingByAlias.getField() : alias;

        }).setFieldValueEditor((field, value) -> {
            FieldAliasMapping fieldAliasMappingByField = entityTransformer.getFieldAliasMappingByField(field);
            return fieldAliasMappingByField != null ? fieldAliasMappingByField.reconstitute(value) : value;
        });
    }

    protected void initTypeAdapter() {
        this.typeAdapter = (persistent) -> type;
    }

    protected void processTypeAdapter() {
        if (typeAdapter instanceof MapTypeAdapter) {
            ((MapTypeAdapter) typeAdapter).initialize(entityElement, entityTransformer);
        }
    }

    @Override
    public Object deserialize(Context context, Object object) {
        return BeanUtil.toBean(object, typeAdapter.determineType(object), copyOptions);
    }

}
