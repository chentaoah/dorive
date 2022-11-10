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
package com.gitee.spring.domain.core.impl.resolver;

import com.gitee.spring.domain.core.annotation.Entity;
import com.gitee.spring.domain.core.entity.Property;
import com.gitee.spring.domain.core.entity.PropertyChain;
import lombok.Data;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ReflectionUtils;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class PropertyResolver {

    private Map<String, PropertyChain> allPropertyChainMap = new LinkedHashMap<>();
    private Map<String, PropertyChain> fieldPropertyChainMap = new LinkedHashMap<>();

    public void resolveProperties(String lastAccessPath, Class<?> entityClass) {
        ReflectionUtils.doWithLocalFields(entityClass, declaredField -> {

            PropertyChain lastPropertyChain = allPropertyChainMap.get(lastAccessPath);

            Property property = new Property(declaredField);
            Class<?> fieldEntityClass = property.getFieldClass();
            String fieldName = property.getFieldName();

            String fieldAccessPath = lastAccessPath + "/" + fieldName;
            boolean isAnnotatedEntity = AnnotatedElementUtils.isAnnotated(declaredField, Entity.class);

            PropertyChain propertyChain = new PropertyChain(
                    lastPropertyChain,
                    entityClass,
                    property,
                    fieldAccessPath,
                    isAnnotatedEntity,
                    null);

            if (isAnnotatedEntity) {
                propertyChain.initialize();
            }

            allPropertyChainMap.put(fieldAccessPath, propertyChain);
            fieldPropertyChainMap.putIfAbsent(fieldName, propertyChain);

            if (!filterEntityClass(fieldEntityClass)) {
                resolveProperties(fieldAccessPath, fieldEntityClass);
            }
        });
    }

    private boolean filterEntityClass(Class<?> entityClass) {
        String className = entityClass.getName();
        return className.startsWith("java.lang.") || className.startsWith("java.util.") || entityClass.isEnum();
    }

}
