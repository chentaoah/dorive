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
package com.gitee.spring.domain.coating.impl.resolver;

import com.gitee.spring.domain.coating.annotation.Coating;
import com.gitee.spring.domain.coating.entity.CoatingWrapper;
import com.gitee.spring.domain.coating.entity.PropertyWrapper;
import com.gitee.spring.domain.coating.entity.RepositoryWrapper;
import com.gitee.spring.domain.coating.entity.definition.CoatingDefinition;
import com.gitee.spring.domain.coating.entity.definition.PropertyDefinition;
import com.gitee.spring.domain.coating.repository.AbstractCoatingRepository;
import com.gitee.spring.domain.coating.util.ResourceUtils;
import com.gitee.spring.domain.core.entity.Property;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class CoatingWrapperResolver {

    private AbstractCoatingRepository<?, ?> repository;

    private Map<Class<?>, CoatingWrapper> coatingWrapperMap = new ConcurrentHashMap<>();

    public CoatingWrapperResolver(AbstractCoatingRepository<?, ?> repository) {
        this.repository = repository;
    }

    public void resolveCoatingWrapperMap(String... scanPackages) throws Exception {
        for (String scanPackage : scanPackages) {
            List<Class<?>> classes = ResourceUtils.resolveClasses(scanPackage);
            for (Class<?> coatingClass : classes) {

                Coating coatingAnnotation = AnnotationUtils.getAnnotation(coatingClass, Coating.class);
                if (coatingAnnotation == null) {
                    continue;
                }
                if (coatingAnnotation.qualifier() != Object.class && coatingAnnotation.qualifier() != repository.getEntityClass()) {
                    continue;
                }

                Map<String, PropertyWrapper> allPropertyWrapperMap = new LinkedHashMap<>();
                Map<String, List<PropertyWrapper>> accessPathPropertyWrappersMap = new LinkedHashMap<>();
                Map<String, PropertyWrapper> fieldPropertyWrapperMap = new LinkedHashMap<>();
                PropertyWrapper[] specificPropertyWrappers = new PropertyWrapper[4];

                ReflectionUtils.doWithLocalFields(coatingClass, declaredField -> {
                    Property property = new Property(declaredField);
                    String fieldName = property.getFieldName();

                    PropertyDefinition propertyDefinition = PropertyDefinition.newPropertyDefinition(declaredField);
                    if (propertyDefinition.isIgnore()) {
                        return;
                    }
                    if (StringUtils.isBlank(propertyDefinition.getAlias())) {
                        propertyDefinition.setAlias(fieldName);
                    }
                    if (StringUtils.isBlank(propertyDefinition.getOperator())) {
                        propertyDefinition.setOperator("=");
                    }

                    PropertyWrapper propertyWrapper = new PropertyWrapper(property, propertyDefinition);
                    if ("orderByAsc".equals(fieldName)) {
                        specificPropertyWrappers[0] = propertyWrapper;
                        return;

                    } else if ("orderByDesc".equals(fieldName)) {
                        specificPropertyWrappers[1] = propertyWrapper;
                        return;

                    } else if ("pageNum".equals(fieldName)) {
                        specificPropertyWrappers[2] = propertyWrapper;
                        return;

                    } else if ("pageSize".equals(fieldName)) {
                        specificPropertyWrappers[3] = propertyWrapper;
                        return;
                    }

                    allPropertyWrapperMap.put(fieldName, propertyWrapper);

                    String accessPath = propertyDefinition.getAccessPath();
                    if (StringUtils.isNotBlank(accessPath) && accessPath.startsWith("/")) {
                        List<PropertyWrapper> propertyWrappers = accessPathPropertyWrappersMap.computeIfAbsent(accessPath, key -> new ArrayList<>());
                        propertyWrappers.add(propertyWrapper);
                    } else {
                        fieldPropertyWrapperMap.put(fieldName, propertyWrapper);
                    }
                });

                RepoDefinitionResolver repoDefinitionResolver = repository.getRepoDefinitionResolver();
                List<RepositoryWrapper> repositoryWrappers = repoDefinitionResolver.collectRepositoryWrappers(accessPathPropertyWrappersMap, fieldPropertyWrapperMap);
                checkFieldNames(coatingClass, allPropertyWrapperMap.keySet(), repositoryWrappers);

                List<RepositoryWrapper> reversedRepositoryWrappers = new ArrayList<>(repositoryWrappers);
                Collections.reverse(reversedRepositoryWrappers);

                CoatingDefinition coatingDefinition = CoatingDefinition.newCoatingDefinition(coatingClass);
                CoatingWrapper coatingWrapper = new CoatingWrapper(
                        coatingDefinition,
                        repositoryWrappers,
                        reversedRepositoryWrappers,
                        specificPropertyWrappers[0],
                        specificPropertyWrappers[1],
                        specificPropertyWrappers[2],
                        specificPropertyWrappers[3]);
                coatingWrapperMap.put(coatingClass, coatingWrapper);
            }
        }
    }

    private void checkFieldNames(Class<?> coatingClass, Set<String> fieldNames, List<RepositoryWrapper> repositoryWrappers) {
        Set<String> remainFieldNames = new LinkedHashSet<>(fieldNames);
        for (RepositoryWrapper repositoryWrapper : repositoryWrappers) {
            for (PropertyWrapper propertyWrapper : repositoryWrapper.getCollectedPropertyWrappers()) {
                remainFieldNames.remove(propertyWrapper.getProperty().getFieldName());
            }
        }
        if (!remainFieldNames.isEmpty()) {
            String errorMessage = String.format("The field does not exist in the aggregate root! entity: %s, coating: %s, fieldNames: %s",
                    repository.getEntityClass(), coatingClass, remainFieldNames);
            throw new RuntimeException(errorMessage);
        }
    }

}
