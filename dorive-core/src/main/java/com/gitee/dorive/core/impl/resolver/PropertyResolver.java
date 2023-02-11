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
package com.gitee.dorive.core.impl.resolver;

import com.gitee.dorive.core.entity.element.PropertyChain;
import com.gitee.dorive.core.entity.definition.EntityDefinition;
import com.gitee.dorive.core.repository.DefaultRepository;
import lombok.Data;
import org.springframework.util.ReflectionUtils;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class PropertyResolver {

    private boolean ignoreEntity;
    private Map<String, PropertyChain> allPropertyChainMap = new LinkedHashMap<>();

    public PropertyResolver(boolean ignoreEntity) {
        this.ignoreEntity = ignoreEntity;
    }

    public void resolveProperties(Class<?> entityClass) {
        resolveProperties("", entityClass);
    }

    public void resolveProperties(String lastAccessPath, Class<?> entityClass) {
        PropertyChain lastPropertyChain = allPropertyChainMap.get(lastAccessPath);
        ReflectionUtils.doWithLocalFields(entityClass, declaredField -> {
            String accessPath = lastAccessPath + "/" + declaredField.getName();

            PropertyChain propertyChain = new PropertyChain(lastPropertyChain, entityClass, accessPath, declaredField);
            allPropertyChainMap.put(accessPath, propertyChain);

            EntityDefinition entityDefinition = propertyChain.getEntityDefinition();
            if (entityDefinition != null) {
                if (ignoreEntity) {
                    return;
                }
                Class<?> repository = entityDefinition.getRepository();
                if (repository != null && repository != DefaultRepository.class) {
                    return;
                }
            }

            Class<?> fieldClass = propertyChain.getFieldClass();
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
