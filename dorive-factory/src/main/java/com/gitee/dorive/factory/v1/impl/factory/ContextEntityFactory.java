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
import com.gitee.dorive.base.v1.common.api.BoundedContext;
import com.gitee.dorive.base.v1.common.def.PropertyDef;
import com.gitee.dorive.base.v1.common.entity.EntityElement;
import com.gitee.dorive.base.v1.common.entity.PropertyDefinition;
import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.factory.v1.api.EntityFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContextEntityFactory implements EntityFactory {

    // 边界上下文
    private String boundedContextName;
    private BoundedContext boundedContext;
    private CopyOptions ctxCopyOptions;
    // 默认实体工厂
    private EntityFactory entityFactory;

    public void initCtxCopyOptions(EntityElement entityElement) {
        List<PropertyDefinition> propertyDefinitions = entityElement.getPropertyDefinitions();
        if (!propertyDefinitions.isEmpty()) {
            Map<String, String> keyFieldNameMap = new ConcurrentHashMap<>(propertyDefinitions.size() * 4 / 3 + 1);
            for (PropertyDefinition propertyDefinition : propertyDefinitions) {
                PropertyDef propertyDef = propertyDefinition.getPropertyDef();
                String key = propertyDef.getValue();
                String fieldName = propertyDefinition.getFieldName();
                keyFieldNameMap.put(key, fieldName);
            }
            this.ctxCopyOptions = CopyOptions.create().ignoreNullValue().setFieldMapping(keyFieldNameMap);
        }
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
        if (boundedContext != null) {
            for (Object persistent : persistentObjs) {
                BeanUtil.copyProperties(boundedContext, persistent, ctxCopyOptions);
            }
        }
        return entityFactory.reconstitute(context, persistentObjs);
    }

    @Override
    public List<Object> deconstruct(Context context, List<?> entities) {
        return entityFactory.deconstruct(context, entities);
    }

}
