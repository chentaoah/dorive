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

package com.gitee.dorive.def.impl;

import com.gitee.dorive.def.entity.EntityDefinition;
import com.gitee.dorive.def.entity.FieldDefinition;
import com.gitee.dorive.def.entity.PropertyChain;
import com.gitee.dorive.def.util.ReflectUtils;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class PropertyChainResolver {

    private Map<String, PropertyChain> propertyChainMap = new LinkedHashMap<>();

    public void resolve(String lastAccessPath, EntityDefinition entityDefinition) {
        PropertyChain lastPropertyChain = propertyChainMap.get(lastAccessPath);
        List<FieldDefinition> fieldDefinitions = entityDefinition.getFieldDefinitions();
        for (FieldDefinition fieldDefinition : fieldDefinitions) {
            String accessPath = lastAccessPath + "/" + fieldDefinition.getName();
            Class<?> entityType = ReflectUtils.getClass(entityDefinition.getGenericTypeName());
            Class<?> fieldType = ReflectUtils.getClass(fieldDefinition.getTypeName());
            PropertyChain propertyChain = new PropertyChain(lastPropertyChain, entityType, accessPath, fieldType);
            propertyChainMap.put(accessPath, propertyChain);
            if (isComplexType(fieldType)) {
                resolveField(accessPath, fieldType);
            }
        }
    }

    private boolean isComplexType(Class<?> fieldType) {
        String className = fieldType.getName();
        return !className.startsWith("java.lang.") && !className.startsWith("java.util.") && !fieldType.isEnum();
    }

    private void resolveField(String lastAccessPath, Class<?> fieldType) {
        Field[] declaredFields = fieldType.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            String accessPath = lastAccessPath + "/" + declaredField.getName();
        }
    }

}
