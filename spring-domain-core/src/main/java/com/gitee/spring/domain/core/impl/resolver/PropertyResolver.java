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
    
    private boolean ignoreAnnotated;
    private Map<String, PropertyChain> allPropertyChainMap = new LinkedHashMap<>();

    public PropertyResolver(boolean ignoreAnnotated) {
        this.ignoreAnnotated = ignoreAnnotated;
    }

    public void resolveProperties(Class<?> entityClass) {
        resolveProperties("", entityClass);
    }

    public void resolveProperties(String lastAccessPath, Class<?> entityClass) {
        PropertyChain lastPropertyChain = allPropertyChainMap.get(lastAccessPath);
        ReflectionUtils.doWithLocalFields(entityClass, declaredField -> {
            String accessPath = lastAccessPath + "/" + declaredField.getName();
            boolean isAnnotatedEntity = AnnotatedElementUtils.isAnnotated(declaredField, Entity.class);
            Property property = new Property(declaredField);

            PropertyChain propertyChain = new PropertyChain(
                    lastPropertyChain,
                    entityClass,
                    accessPath,
                    isAnnotatedEntity,
                    property,
                    null);

            if (isAnnotatedEntity) {
                propertyChain.initialize();
            }

            allPropertyChainMap.put(accessPath, propertyChain);

            if (ignoreAnnotated && isAnnotatedEntity) {
                return;
            }

            Class<?> fieldClass = property.getFieldClass();
            if (!filterEntityClass(fieldClass)) {
                resolveProperties(accessPath, fieldClass);
            }
        });
    }

    private boolean filterEntityClass(Class<?> entityClass) {
        String className = entityClass.getName();
        return className.startsWith("java.lang.") || className.startsWith("java.util.") || entityClass.isEnum();
    }

}
