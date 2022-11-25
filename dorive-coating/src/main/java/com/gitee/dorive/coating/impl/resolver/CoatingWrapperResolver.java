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
package com.gitee.dorive.coating.impl.resolver;

import com.gitee.dorive.coating.annotation.Coating;
import com.gitee.dorive.coating.entity.CoatingWrapper;
import com.gitee.dorive.coating.entity.MergedRepository;
import com.gitee.dorive.coating.entity.PropertyWrapper;
import com.gitee.dorive.coating.entity.RepositoryWrapper;
import com.gitee.dorive.coating.entity.SpecificProperties;
import com.gitee.dorive.coating.repository.AbstractCoatingRepository;
import com.gitee.spring.domain.coating.entity.*;
import com.gitee.dorive.coating.entity.definition.CoatingDefinition;
import com.gitee.dorive.coating.entity.definition.PropertyDefinition;
import com.gitee.dorive.coating.util.ResourceUtils;
import com.gitee.dorive.core.entity.EntityElement;
import com.gitee.dorive.core.entity.Property;
import com.gitee.dorive.core.repository.ConfiguredRepository;
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

                Set<String> fieldNames = new LinkedHashSet<>();
                Map<String, List<PropertyWrapper>> accessPathPropertyWrappersMap = new LinkedHashMap<>();
                Map<String, PropertyWrapper> fieldPropertyWrapperMap = new LinkedHashMap<>();
                SpecificProperties specificProperties = new SpecificProperties();

                ReflectionUtils.doWithLocalFields(coatingClass, declaredField -> {
                    Property property = new Property(declaredField);
                    String fieldName = property.getFieldName();

                    PropertyDefinition propertyDefinition = PropertyDefinition.newPropertyDefinition(declaredField);
                    PropertyDefinition.renewPropertyDefinition(fieldName, propertyDefinition);
                    if (propertyDefinition.isIgnore()) {
                        return;
                    }

                    PropertyWrapper propertyWrapper = new PropertyWrapper(property, propertyDefinition);
                    if (specificProperties.addProperty(fieldName, propertyWrapper)) {
                        return;
                    }

                    fieldNames.add(fieldName);

                    String accessPath = propertyDefinition.getAccessPath();
                    if (StringUtils.isNotBlank(accessPath) && accessPath.startsWith("/")) {
                        List<PropertyWrapper> existPropertyWrappers = accessPathPropertyWrappersMap.computeIfAbsent(accessPath, key -> new ArrayList<>());
                        existPropertyWrappers.add(propertyWrapper);
                    } else {
                        fieldPropertyWrapperMap.put(fieldName, propertyWrapper);
                    }
                });

                List<RepositoryWrapper> repositoryWrappers = collectRepositoryWrappers(accessPathPropertyWrappersMap, fieldPropertyWrapperMap);
                checkFieldNames(coatingClass, fieldNames, repositoryWrappers);

                List<RepositoryWrapper> reversedRepositoryWrappers = new ArrayList<>(repositoryWrappers);
                Collections.reverse(reversedRepositoryWrappers);

                CoatingDefinition coatingDefinition = CoatingDefinition.newCoatingDefinition(coatingClass);
                CoatingWrapper coatingWrapper = new CoatingWrapper(coatingDefinition, repositoryWrappers, reversedRepositoryWrappers, specificProperties);
                coatingWrapperMap.put(coatingClass, coatingWrapper);
            }
        }
    }

    private List<RepositoryWrapper> collectRepositoryWrappers(Map<String, List<PropertyWrapper>> accessPathPropertyWrappersMap,
                                                              Map<String, PropertyWrapper> fieldPropertyWrapperMap) {
        MergedRepositoryResolver mergedRepositoryResolver = repository.getMergedRepositoryResolver();
        Map<String, MergedRepository> mergedRepositoryMap = mergedRepositoryResolver.getMergedRepositoryMap();

        List<RepositoryWrapper> repositoryWrappers = new ArrayList<>();

        for (MergedRepository mergedRepository : mergedRepositoryMap.values()) {
            String absoluteAccessPath = mergedRepository.getAbsoluteAccessPath();
            ConfiguredRepository repository = mergedRepository.getConfiguredRepository();
            EntityElement entityElement = repository.getEntityElement();

            List<PropertyWrapper> propertyWrappers = new ArrayList<>();

            List<PropertyWrapper> accessPathPropertyWrappers = accessPathPropertyWrappersMap.get(absoluteAccessPath);
            if (accessPathPropertyWrappers != null) {
                propertyWrappers.addAll(accessPathPropertyWrappers);
            }

            for (String fieldName : entityElement.getProperties()) {
                PropertyWrapper propertyWrapper = fieldPropertyWrapperMap.get(fieldName);
                if (propertyWrapper != null) {
                    propertyWrappers.add(propertyWrapper);
                }
            }

            if (!propertyWrappers.isEmpty() || repository.isBoundEntity()) {
                RepositoryWrapper repositoryWrapper = new RepositoryWrapper(mergedRepository, propertyWrappers);
                repositoryWrappers.add(repositoryWrapper);
            }
        }

        return repositoryWrappers;
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
