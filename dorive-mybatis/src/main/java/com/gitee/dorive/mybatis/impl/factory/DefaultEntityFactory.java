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

package com.gitee.dorive.mybatis.impl.factory;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.gitee.dorive.api.entity.core.EntityElement;
import com.gitee.dorive.api.entity.core.PropertyDefinition;
import com.gitee.dorive.api.entity.core.def.PropertyDef;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.factory.EntityAdapter;
import com.gitee.dorive.core.api.factory.EntityFactory;
import com.gitee.dorive.core.api.factory.EntityMapper;
import com.gitee.dorive.api.entity.common.BoundedContext;
import com.gitee.dorive.mybatis.entity.common.EntityStoreInfo;
import com.gitee.dorive.core.entity.enums.Domain;
import com.gitee.dorive.core.entity.factory.FieldConverter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DefaultEntityFactory implements EntityFactory {

    private EntityElement entityElement;
    private EntityStoreInfo entityStoreInfo;
    // 序列化
    private EntityMapper entityMapper;
    private CopyOptions reCopyOptions;
    private CopyOptions deCopyOptions;
    // 边界上下文
    private String boundedContextName;
    private BoundedContext boundedContext;
    private CopyOptions ctxCopyOptions;
    // 适配器
    private EntityAdapter entityAdapter;

    public void setEntityElement(EntityElement entityElement) {
        this.entityElement = entityElement;
        initCtxCopyOptions();
        initEntityAdapter();
    }

    private void initCtxCopyOptions() {
        List<PropertyDefinition> propertyDefinitions = entityElement.getPropertyDefinitions();
        if (!propertyDefinitions.isEmpty()) {
            Map<String, String> keyFieldNameMapping = new ConcurrentHashMap<>(propertyDefinitions.size() * 4 / 3 + 1);
            for (PropertyDefinition propertyDefinition : propertyDefinitions) {
                PropertyDef propertyDef = propertyDefinition.getPropertyDef();
                String key = propertyDef.getValue();
                String fieldName = propertyDefinition.getFieldName();
                keyFieldNameMapping.put(key, fieldName);
            }
            this.ctxCopyOptions = CopyOptions.create().ignoreNullValue().setFieldMapping(keyFieldNameMapping);
        }
    }

    protected void initEntityAdapter() {
        this.entityAdapter = (persistent) -> entityElement.getGenericType();
    }

    public void setEntityMapper(EntityMapper entityMapper) {
        this.entityMapper = entityMapper;
        initReCopyOptions();
        initDeCopyOptions();
    }

    private void initReCopyOptions() {
        this.reCopyOptions = CopyOptions.create().ignoreNullValue().setFieldNameEditor(name -> {
            FieldConverter fieldConverter = entityMapper.getField(Domain.DATABASE.name(), name);
            return fieldConverter != null ? fieldConverter.getName() : name;

        }).setFieldValueEditor((name, value) -> {
            FieldConverter fieldConverter = entityMapper.getField(Domain.ENTITY.name(), name);
            return fieldConverter != null ? fieldConverter.reconstitute(value) : value;
        });
    }

    private void initDeCopyOptions() {
        this.deCopyOptions = CopyOptions.create().ignoreNullValue().setFieldNameEditor(name -> {
            FieldConverter fieldConverter = entityMapper.getField(Domain.ENTITY.name(), name);
            return fieldConverter != null ? fieldConverter.getName(Domain.POJO.name()) : name;

        }).setFieldValueEditor((name, value) -> {
            FieldConverter fieldConverter = entityMapper.getField(Domain.POJO.name(), name);
            return fieldConverter != null ? fieldConverter.deconstruct(value) : value;
        });
    }

    @Override
    public List<Object> reconstitute(Context context, List<?> persistentObjs) {
        BoundedContext boundedContext = null;
        if (ctxCopyOptions != null) {
            Object attachment = context.getAttachment(boundedContextName);
            if (attachment instanceof BoundedContext) {
                boundedContext = (BoundedContext) attachment;
            }
            if (boundedContext == null) {
                boundedContext = this.boundedContext;
            }
        }
        List<Object> entities = new ArrayList<>(persistentObjs.size());
        if (boundedContext == null) {
            for (Object persistent : persistentObjs) {
                Object entity = reconstitute(context, persistent);
                entities.add(entity);
            }
        } else {
            for (Object persistent : persistentObjs) {
                Object entity = reconstitute(context, persistent);
                BeanUtil.copyProperties(boundedContext, entity, ctxCopyOptions);
                entities.add(entity);
            }
        }
        return entities;
    }

    public Object reconstitute(Context context, Object persistent) {
        return BeanUtil.toBean(persistent, entityAdapter.adaptEntityType(persistent), reCopyOptions);
    }

    @Override
    public List<Object> deconstruct(Context context, List<?> entities) {
        List<Object> persistentObjs = new ArrayList<>(entities.size());
        for (Object entity : entities) {
            Object persistent = deconstruct(context, entity);
            persistentObjs.add(persistent);
        }
        return persistentObjs;
    }

    public Object deconstruct(Context context, Object entity) {
        return BeanUtil.toBean(entity, entityStoreInfo.getPojoClass(), deCopyOptions);
    }

}
